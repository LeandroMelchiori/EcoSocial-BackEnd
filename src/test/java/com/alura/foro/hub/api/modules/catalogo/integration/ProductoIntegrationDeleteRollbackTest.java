package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import com.alura.foro.hub.api.modules.catalogo.repository.CategoriaCatalogoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.SubCategoriaCatalogoRepository;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoIntegrationDeleteRollbackTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaCatalogoRepository categoriaRepository;
    @Autowired SubCategoriaCatalogoRepository subcategoriaRepository;

    // 👇 IMPORTANTÍSIMO: Spy para forzar falla DB en el delete
    @MockitoSpyBean
    ProductoRepository productoRepository;

    @Value("${catalogo.local.root:uploads}")
    String uploadsRoot;

    private Path uploadsPath;

    private Usuario usuario;
    private CategoriaCatalogo categoria;
    private Subcategoria subcategoria;

    @BeforeEach
    void setup() throws Exception {
        uploadsPath = Paths.get(uploadsRoot).toAbsolutePath().normalize();
        purgeDir(uploadsPath);

        // Usuario
        usuario = new Usuario();
        usuario.setUsername("user_test_" + UUID.randomUUID());
        usuario.setEmail(usuario.getUsername() + "@mail.com");
        usuario.setNombre("User Test");
        usuario.setPassword("123456");
        usuario = usuarioRepository.save(usuario);

        // Categoria
        categoria = new CategoriaCatalogo();
        categoria.setNombre("Tecnología");
        categoria = categoriaRepository.save(categoria);

        // Subcategoria
        subcategoria = new Subcategoria();
        subcategoria.setNombre("Celulares");
        subcategoria.setCategoria(categoria); // ajustá si tu setter se llama distinto
        subcategoria = subcategoriaRepository.save(subcategoria);

        // Limpio stubs entre tests (por las dudas)
        Mockito.reset(productoRepository);
    }

    @AfterEach
    void teardown() throws Exception {
        purgeDir(uploadsPath);
    }

    @Test
    @Order(1)
    void delete_si_falla_db_debe_rollback_y_no_borrar_imagenes() throws Exception {
        // 1) Crear producto con imágenes
        Long productoId = crearProductoConImagenes(2);

        Path productoDir = uploadsPath.resolve(Paths.get("productos", String.valueOf(productoId)));
        assertThat(Files.exists(productoDir)).isTrue();

        List<Path> imagenesAntes = listarArchivos(productoDir);
        assertThat(imagenesAntes).hasSize(2);

        // 2) Forzar falla DB cuando el service intente borrar en repo
        // (cubrimos deleteById y delete(entity), no sabemos cuál usa tu service)
        Mockito.doThrow(new RuntimeException("FALLA_FORZADA_DB_DELETE"))
                .when(productoRepository).deleteById(productoId);

        Mockito.doThrow(new RuntimeException("FALLA_FORZADA_DB_DELETE"))
                .when(productoRepository).delete(any());

        // 3) Ejecutar DELETE -> debe dar 5xx (porque explotó adentro)
        mvc.perform(
                        delete("/catalogo/productos/{id}", productoId)
                                // auth “mock”: importante que getName() sea tu username
                                .with(user(usuario.getUsername()).roles("ADMIN"))
                )
                .andExpect(status().is5xxServerError());

        // 4) Assert DB: producto sigue existiendo (rollback)
        assertThat(productoRepository.existsById(productoId))
                .as("Si falla DB, NO debería borrarse el producto")
                .isTrue();

        // 5) Assert FS: imágenes siguen existiendo (NO se pierden)
        List<Path> imagenesDespues = listarArchivos(productoDir);
        assertThat(imagenesDespues).hasSize(2);

        for (Path img : imagenesAntes) {
            assertThat(Files.exists(img))
                    .as("La imagen NO debería borrarse ante rollback: " + img.getFileName())
                    .isTrue();
        }

        // 6) Assert NO quedó basura en temp/trash (si tu implementación usa esos dirs)
        Path tempDir = uploadsPath.resolve("temp");
        if (Files.exists(tempDir)) {
            List<Path> tempFiles = Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            assertThat(tempFiles).isEmpty();
        }

        Path trashDir = uploadsPath.resolve("trash");
        if (Files.exists(trashDir)) {
            List<Path> trashFiles = Files.walk(trashDir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            assertThat(trashFiles).isEmpty();
        }

        // 7) sanity: GET sigue respondiendo OK (el recurso vive)
        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId));
    }

    // -------------------------
    // helpers (igual estilo al update test)
    // -------------------------

    private Long crearProductoConImagenes(int cantidadImgs) throws Exception {

        String crearJson = """
            {
              "categoriaCatalogoId": %d,
              "subCategoriaCatalogoId": %d,
              "titulo": "Producto Para Delete",
              "descripcion": "Producto inicial para test delete rollback"
            }
            """.formatted(categoria.getId(), subcategoria.getId());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                "application/json",
                crearJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartHttpServletRequestBuilder builder =
                multipart("/catalogo/productos")
                        .file(data);

        for (int i = 1; i <= cantidadImgs; i++) {
            builder.file(new MockMultipartFile(
                    "imagenes",
                    "img" + i + ".png",
                    "image/png",
                    fakePngBytes("IMG_" + i)
            ));
        }

        // auth mock
        builder.with(user(usuario.getUsername()).roles("ADMIN"))
                .contentType(MediaType.MULTIPART_FORM_DATA);

        var result = mvc.perform(builder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        return created.get("id").asLong();
    }

    private static byte[] fakePngBytes(String seed) {
        return ("PNG_BYTES_" + seed + "_" + UUID.randomUUID())
                .getBytes(StandardCharsets.UTF_8);
    }

    private static List<Path> listarArchivos(Path dir) throws Exception {
        if (!Files.exists(dir)) return new ArrayList<>();
        try (var s = Files.list(dir)) {
            return s.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    private static void purgeDir(Path dir) throws Exception {
        if (dir == null || !Files.exists(dir)) return;
        Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
    }
}
