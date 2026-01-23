package com.alura.foro.hub.api.modules.catalogo.unit.producto;

import com.alura.foro.hub.api.modules.catalogo.domain.*;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.repository.*;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.modules.catalogo.service.MinioStorageService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Perfil;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepo;
    @Mock private CategoriaCatalogoRepository categoriaRepo;
    @Mock private SubCategoriaCatalogoRepository subcategoriaRepo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private MinioStorageService storage;
    @Mock private ProductoImagenRepository productoImagenRepo;
    @Mock private EntityManager em;

    @InjectMocks
    private ProductoService service;


    @BeforeEach
    void setUp() {
        // Porque en tu service el EntityManager esta con @PersistenceContext (field injection),
        // y Mockito no siempre lo mete bien con @InjectMocks.
        ReflectionTestUtils.setField(service, "em", em);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Perfil perfil(String nombre) {
        Perfil p = new Perfil();
        p.setNombre(nombre);
        return p;
    }

    private Usuario user(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setPerfiles(new ArrayList<>()); // sin perfiles => ROLE_USER por defecto
        return u;
    }

    private Usuario admin(Long id) {
        Usuario u = user(id);
        u.getPerfiles().add(perfil("ADMIN")); // => ROLE_ADMIN en getAuthorities()
        return u;
    }

    private Producto productoConOwner(Long productoId, Long ownerId) {
        Producto p = new Producto();
        p.setId(productoId);
        p.setUsuario(user(ownerId));
        p.setImagenes(new ArrayList<>());
        p.setActivo(true);
        p.setTitulo("t");
        p.setDescripcion("d");
        p.setFechaCreacion(java.time.LocalDateTime.now());
        return p;
    }

    private ProductoImagen imagen(Long id, int orden, String key) {
        ProductoImagen img = new ProductoImagen();
        img.setId(id);
        img.setOrden(orden);
        img.setUrl(key);
        return img;
    }

    private void withTxSync(Runnable r) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            r.run();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private List<TransactionSynchronization> syncs() {
        return TransactionSynchronizationManager.getSynchronizations();
    }

    // ----------------------------------------------------------------
    // Tests: CREAR (validaciones basicas)
    // ----------------------------------------------------------------

    @Test
    void crear_falla_si_categoria_no_existe() throws Exception {
        var dto = new DatosCrearProducto(1L, null, "Titulo", "Descripcion");

        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(categoriaRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.crear(dto, null, 1L));
    }

    @Test
    void crear_falla_si_imagen_tipo_no_permitido() throws Exception {
        var dto = new DatosCrearProducto(1L, null, "Titulo", "Descripcion");

        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(new CategoriaCatalogo()));

        var bad = new MockMultipartFile(
                "imagenes", "a.gif", "image/gif", "x".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(BadRequestException.class, () -> service.crear(dto, List.of(bad), 1L));
    }

    // ----------------------------------------------------------------
    // Tests: ELIMINAR (permisos + commit/rollback MinIO)
    // ----------------------------------------------------------------

    @Test
    void eliminar_falla_si_no_es_duenio_ni_admin() {
        Producto p = productoConOwner(10L, 1L);

        when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(user(2L)));

        assertThrows(ForbiddenException.class, () -> service.eliminar(10L, 2L));
        verify(productoRepo, never()).delete(any());
    }

    @Test
    void eliminar_ok_si_es_duenio() {
        Producto p = productoConOwner(10L, 1L);

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(storage.moveProductDirToTrash(eq(10L), anyString())).thenReturn("trash/op/productos/10/");

            service.eliminar(10L, 1L);

            verify(storage).moveProductDirToTrash(eq(10L), anyString());
            verify(productoRepo).delete(p);
            verify(em).flush();

            // Simulamos commit: afterCommit debe purgar trash
            assertFalse(syncs().isEmpty());
            syncs().forEach(TransactionSynchronization::afterCommit);
            verify(storage).purgeTrash("trash/op/productos/10/");
        });
    }

    @Test
    void eliminar_ok_si_es_admin() {
        Producto p = productoConOwner(10L, 1L);

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(99L)).thenReturn(Optional.of(admin(99L)));
            when(storage.moveProductDirToTrash(eq(10L), anyString())).thenReturn("trash/op/productos/10/");

            service.eliminar(10L, 99L);

            verify(productoRepo).delete(p);
            verify(em).flush();
        });
    }

    @Test
    void eliminar_rollback_restaurar_trash_si_falla_flush() {
        Producto p = productoConOwner(10L, 1L);

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(storage.moveProductDirToTrash(eq(10L), anyString())).thenReturn("trash/op/productos/10/");
            doThrow(new RuntimeException("FK constraint")).when(em).flush();

            assertThrows(RuntimeException.class, () -> service.eliminar(10L, 1L));

            // Simulamos rollback: afterCompletion != COMMITTED => restaurar
            assertFalse(syncs().isEmpty());
            syncs().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

            verify(storage).restoreTrashToProductDir(10L, "trash/op/productos/10/");
            verify(storage, never()).purgeTrash(anyString());
        });
    }

    // ----------------------------------------------------------------
    // Tests: REORDENAR IMAGENES
    // ----------------------------------------------------------------

    @Test
    void reordenar_falla_si_ids_no_coinciden_con_cantidad() {
        Producto p = productoConOwner(1L, 1L);
        p.getImagenes().add(imagen(11L, 1, "k1"));
        p.getImagenes().add(imagen(12L, 2, "k2"));

        when(productoRepo.findWithImagenesById(1L)).thenReturn(Optional.of(p));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(admin(1L)));

        var dto = new DatosReordenarImagenes(List.of(11L)); // falta una

        assertThrows(BadRequestException.class, () -> service.reordenarImagenes(1L, dto, 1L));
    }

    @Test
    void reordenar_falla_si_ids_no_son_los_actuales() {
        Producto p = productoConOwner(1L, 1L);
        p.getImagenes().add(imagen(11L, 1, "k1"));
        p.getImagenes().add(imagen(12L, 2, "k2"));

        when(productoRepo.findWithImagenesById(1L)).thenReturn(Optional.of(p));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(admin(1L)));

        var dto = new DatosReordenarImagenes(List.of(11L, 99L)); // 99 no existe

        assertThrows(BadRequestException.class, () -> service.reordenarImagenes(1L, dto, 1L));
    }

    @Test
    void reordenar_ok_aplica_nuevo_orden() {
        Producto p = productoConOwner(1L, 1L);
        var img1 = imagen(11L, 1, "k1");
        var img2 = imagen(12L, 2, "k2");
        var img3 = imagen(13L, 3, "k3");
        p.getImagenes().addAll(List.of(img1, img2, img3));

        when(productoRepo.findWithImagenesById(1L)).thenReturn(Optional.of(p));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(admin(1L)));

        var dto = new DatosReordenarImagenes(List.of(13L, 11L, 12L));

        service.reordenarImagenes(1L, dto, 1L);

        assertEquals(2, img1.getOrden()); // 11 va 2do
        assertEquals(3, img2.getOrden()); // 12 va 3ro
        assertEquals(1, img3.getOrden()); // 13 va 1ro

        verify(em, times(2)).flush(); // flush tmp + flush final
    }

    // ----------------------------------------------------------------
    // Tests: ELIMINAR IMAGEN (reordena y borra en minio post-commit)
    // ----------------------------------------------------------------

    @Test
    void eliminarImagen_ok_reordena_y_borra_minio_post_commit() {
        Producto p = productoConOwner(10L, 1L);
        var img1 = imagen(101L, 1, "productos/10/img_1.png");
        var img2 = imagen(102L, 2, "productos/10/img_2.png");
        var img3 = imagen(103L, 3, "productos/10/img_3.png");
        p.getImagenes().addAll(List.of(img1, img2, img3));

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));

            service.eliminarImagen(10L, 102L, 1L);

            // img2 se elimina
            assertEquals(2, p.getImagenes().size());
            assertTrue(p.getImagenes().stream().noneMatch(i -> i.getId().equals(102L)));

            // post-commit borra key
            assertFalse(syncs().isEmpty());
            syncs().forEach(TransactionSynchronization::afterCommit);

            verify(storage).deleteObjects(List.of("productos/10/img_2.png"));
        });
    }

    // ----------------------------------------------------------------
    // Tests: REEMPLAZAR IMAGEN (borra vieja post-commit / borra nueva si rollback)
    // ----------------------------------------------------------------

    @Test
    void reemplazarImagen_ok_borra_vieja_post_commit() {
        Producto p = productoConOwner(10L, 1L);
        var img1 = imagen(101L, 1, "productos/10/old.png");
        p.getImagenes().add(img1);

        var nueva = new MockMultipartFile(
                "imagen", "n.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(storage.saveProductImage(eq(10L), any(), eq(1)))
                    .thenReturn("productos/10/new.png");

            try {
                service.reemplazarImagen(10L, 101L, nueva, 1L);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertEquals("productos/10/new.png", img1.getUrl());

            // commit => borra vieja
            syncs().forEach(TransactionSynchronization::afterCommit);
            verify(storage).deleteObjects(List.of("productos/10/old.png"));
        });
    }

    @Test
    void reemplazarImagen_rollback_borra_nueva_para_no_dejar_basura() {
        Producto p = productoConOwner(10L, 1L);
        var img1 = imagen(101L, 1, "productos/10/old.png");
        p.getImagenes().add(img1);

        var nueva = new MockMultipartFile(
                "imagen", "n.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        withTxSync(() -> {
            when(productoRepo.findWithImagenesById(10L)).thenReturn(Optional.of(p));
            when(usuarioRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(storage.saveProductImage(eq(10L), any(), eq(1)))
                    .thenReturn("productos/10/new.png");

            try {
                service.reemplazarImagen(10L, 101L, nueva, 1L);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // simulamos rollback => afterCompletion(ROLLED_BACK) borra la nueva
            syncs().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(storage).deleteObjects(List.of("productos/10/new.png"));
        });
    }

}
