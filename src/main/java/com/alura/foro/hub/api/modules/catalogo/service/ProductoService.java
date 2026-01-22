package com.alura.foro.hub.api.modules.catalogo.service;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.mapper.ProductoMapper;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Usuario; // ajustá paquete
import com.alura.foro.hub.api.user.repository.UsuarioRepository; // ajustá paquete
import com.alura.foro.hub.api.modules.catalogo.domain.*;
import com.alura.foro.hub.api.modules.catalogo.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProductoService {

    @PersistenceContext
    private EntityManager em;

    private static final int MAX_IMGS = 5;
    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    private final ProductoRepository productoRepository;
    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final SubCategoriaCatalogoRepository subCategoriaCatalogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoImagenRepository productoImagenRepository;
    private final StorageService storageService;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaCatalogoRepository categoriaCatalogoRepository,
                           SubCategoriaCatalogoRepository subCategoriaCatalogoRepository,
                           UsuarioRepository usuarioRepository,
                           StorageService storageService,
                           ProductoImagenRepository productoImagenRepository) {
        this.productoRepository = productoRepository;
        this.categoriaCatalogoRepository = categoriaCatalogoRepository;
        this.subCategoriaCatalogoRepository = subCategoriaCatalogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.storageService = storageService;
        this.productoImagenRepository = productoImagenRepository;
    }

    // =========================
    //      CREAR
    // =========================
    @Transactional
    public DatosDetalleProducto crear(DatosCrearProducto dto, List<MultipartFile> imagenes, Long userId) throws IOException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findById(dto.categoriaCatalogoId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        Subcategoria subcategoria = null;
        if (dto.subCategoriaCatalogoId() != null) {
            subcategoria = subCategoriaCatalogoRepository.findById(dto.subCategoriaCatalogoId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));

            if (!subcategoria.getCategoria().getId().equals(categoria.getId())) {
                throw new BadRequestException("La subcategoría no pertenece a la categoría indicada");
            }
        }

        if (imagenes != null) {
            if (imagenes.size() > MAX_IMGS)
                throw new BadRequestException("Máximo " + MAX_IMGS + " imágenes");

            for (MultipartFile f : imagenes) {
                if (f == null || f.isEmpty()) continue;
                if (f.getSize() > MAX_SIZE)
                    throw new BadRequestException("Imagen supera 5MB");

                String ct = f.getContentType();
                if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"))) {
                    throw new BadRequestException("Tipo de imagen no permitido (solo jpg/png/webp)");
                }
            }
        }

        Producto p = new Producto();
        p.setUsuario(usuario);
        p.setCategoria(categoria);
        p.setSubcategoria(subcategoria);
        p.setTitulo(dto.titulo().trim());
        p.setDescripcion(dto.descripcion().trim());
        p.setActivo(true);

        p = productoRepository.save(p);

        if (imagenes != null) {
            int orden = 1;
            for (MultipartFile f : imagenes) {
                if (f == null || f.isEmpty()) continue;

                String url = storageService.saveProductImage(p.getId(), f, orden);

                ProductoImagen img = new ProductoImagen();
                img.setProducto(p);
                img.setOrden(orden);
                img.setUrl(url);

                p.getImagenes().add(img);
                orden++;
            }
            p = productoRepository.save(p);
        }

        return ProductoMapper.toDetalle(p, storageService);

    }

    @Transactional
    public DatosDetalleProducto agregarImagenes(Long productoId, List<MultipartFile> imagenes, Long userId) throws IOException {

        if (imagenes == null || imagenes.isEmpty()) {
            throw new BadRequestException("No enviaste imágenes");
        }

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        validarPermisoProducto(p, userId);
        validarImagenes(imagenes);

        int existentes = (int) p.getImagenes().stream().count();
        int nuevasValidas = (int) imagenes.stream().filter(f -> f != null && !f.isEmpty()).count();

        if (existentes + nuevasValidas > MAX_IMGS) {
            throw new BadRequestException("Máximo " + MAX_IMGS + " imágenes. Ya tenés " + existentes);
        }

        int orden = existentes + 1;
        List<String> newKeys = new java.util.ArrayList<>();

        try {
            for (MultipartFile f : imagenes) {
                if (f == null || f.isEmpty()) continue;

                String key = storageService.saveProductImage(productoId, f, orden);
                newKeys.add(key);

                ProductoImagen img = new ProductoImagen();
                img.setProducto(p);
                img.setOrden(orden++);
                img.setUrl(key);

                p.getImagenes().add(img);
            }

            productoRepository.save(p);
            em.flush();

        } catch (Exception e) {
            // si algo explota antes del commit, limpiamos lo subido
            storageService.deleteObjects(newKeys);
            throw e;
        }

        return ProductoMapper.toDetalle(p, storageService);
    }

    // =========================
    //      LISTAR
    // =========================
    @Transactional(readOnly = true)
    public DatosDetalleProducto detalle(Long id) {
        Producto p = productoRepository.findWithImagenesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        return ProductoMapper.toDetalle(p, storageService);
    }

    @Transactional(readOnly = true)
    public Page<DatosListadoProducto> listar(Long categoriaId, Long subcategoriaId, String q, Pageable pageable) {
        return productoRepository.buscar(categoriaId, subcategoriaId, normalizarQ(q), pageable);
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminar(Long productoId, Long userId) {

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!user.esAdmin() && !p.getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("No tenés permiso para eliminar este producto");
        }

        // 1) mover carpeta a TRASH (si falla acá, no tocamos DB)
        String opId = UUID.randomUUID().toString();
        String trashPrefix = storageService.moveProductDirToTrash(productoId, opId);

        // 2) callbacks de transacción
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // commit OK => borramos definitivo el trash
                storageService.purgeTrash(trashPrefix);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    // rollback => restauramos lo movido
                    storageService.restoreTrashToProductDir(productoId, trashPrefix);
                }
            }
        });

        // 3) borrar en DB
        // importante: delete del producto debería cascader imágenes (orphanRemoval/cascade)
        productoRepository.delete(p);

        // 4) fuerza el DELETE AHORA. Si hay FK/constraint, explota acá y se ejecuta rollback => restaura MinIO
        em.flush();
    }

    @Transactional
    public void eliminarImagen(Long productoId, Long imagenId, Long userId) {

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        validarPermisoProducto(p, userId);

        ProductoImagen target = p.getImagenes().stream()
                .filter(img -> img.getId().equals(imagenId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada en este producto"));

        String objectKey = target.getUrl(); // objectKey en DB

        // 1) sacar de la lista (orphanRemoval)
        p.getImagenes().remove(target);
        productoRepository.save(p);
        em.flush();

        // 2) reordenar 1..n (sin chocar unique)
        reordenarSeguro(p);

        // 3) borrar en MinIO post-commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageService.deleteObjects(List.of(objectKey));
            }
        });
    }

    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosDetalleProducto actualizar(
            Long productoId,
            DatosActualizarProducto dto,
            List<MultipartFile> imagenes,
            Long userId
    ) throws IOException {

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!user.esAdmin() && !p.getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("No tenés permiso para editar este producto");
        }

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findById(dto.categoriaCatalogoId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        Subcategoria subcategoria = null;
        if (dto.subCategoriaCatalogoId() != null) {
            subcategoria = subCategoriaCatalogoRepository.findById(dto.subCategoriaCatalogoId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));

            if (!subcategoria.getCategoria().getId().equals(categoria.getId())) {
                throw new BadRequestException("La subcategoría no pertenece a la categoría indicada");
            }
        }

        // campos simples
        p.setCategoria(categoria);
        p.setSubcategoria(subcategoria);
        p.setTitulo(dto.titulo().trim());
        p.setDescripcion(dto.descripcion().trim());

        // si no mandan imágenes -> solo texto/categoría
        if (imagenes == null  || imagenes.isEmpty()) {
            Producto guardado = productoRepository.save(p);
            return ProductoMapper.toDetalle(guardado, storageService);
        }

        validarImagenes(imagenes);

        // keys viejas (para borrar después del commit)
        List<String> keysViejas = p.getImagenes().stream()
                .map(ProductoImagen::getUrl)
                .toList();

        String opId = UUID.randomUUID().toString();

        // 1) subir a TEMP
        List<String> keysTemp = storageService.uploadProductImagesTemp(productoId, imagenes, opId);

        // 2) copiar TEMP -> FINAL (mismo orden)
        List<String> keysFinal = storageService.promoteTempToFinal(productoId, opId, keysTemp);

        // 3) DB: reemplazar imágenes SIN bulk delete
        //    (orphanRemoval + flush para garantizar orden y evitar UNIQUE)
        p.getImagenes().clear();
        em.flush(); // <-- CLAVE: ejecuta deletes de orphans antes de inserts

        int orden = 1;
        for (String keyFinal : keysFinal) {
            ProductoImagen img = new ProductoImagen();
            img.setProducto(p);
            img.setOrden(orden++);
            img.setUrl(keyFinal);
            p.getImagenes().add(img);
        }

        Producto guardado = productoRepository.save(p);

        // 4) afterCommit/rollback coordinando MinIO
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageService.deleteObjects(keysViejas);
                storageService.purgeTemp(opId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storageService.deleteObjects(keysFinal);
                    storageService.purgeTemp(opId);
                }
            }
        });

        return ProductoMapper.toDetalle(guardado, storageService);
    }

    @Transactional
    public DatosDetalleProducto reordenarImagenes(Long productoId, DatosReordenarImagenes dto, Long userId) {

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!user.esAdmin() && !p.getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("No tenés permiso para editar este producto");
        }

        var ids = dto.orden();
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("Debés enviar el orden de imágenes");
        }

        var actuales = p.getImagenes();
        if (ids.size() != actuales.size()) {
            throw new BadRequestException("El orden enviado no coincide con la cantidad de imágenes actuales");
        }

        var setActual = actuales.stream().map(ProductoImagen::getId).collect(java.util.stream.Collectors.toSet());
        var setNuevo  = new java.util.HashSet<>(ids);

        if (!setActual.equals(setNuevo)) {
            throw new BadRequestException("El orden enviado contiene imágenes inválidas o faltantes");
        }

        // 1) orden temporal para no chocar con uq(producto_id, orden)
        int tmp = 1000;
        for (ProductoImagen img : actuales) {
            img.setOrden(tmp++);
        }
        em.flush();

        // 2) aplicar orden final
        java.util.Map<Long, Integer> pos = new java.util.HashMap<>();
        for (int i = 0; i < ids.size(); i++) pos.put(ids.get(i), i + 1);

        for (ProductoImagen img : actuales) {
            img.setOrden(pos.get(img.getId()));
        }

        productoRepository.save(p);
        em.flush();

        return ProductoMapper.toDetalle(p, storageService);
    }

    @Transactional
    public DatosDetalleProducto reemplazarImagen(Long productoId, Long imagenId, MultipartFile nueva, Long userId) throws IOException {

        Producto p = productoRepository.findWithImagenesById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        validarPermisoProducto(p, userId);
        validarUnaImagen(nueva);

        ProductoImagen target = p.getImagenes().stream()
                .filter(img -> img.getId().equals(imagenId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada en este producto"));

        String oldKey = target.getUrl();
        int orden = target.getOrden();

        // subimos nueva al mismo “orden” (nuevo nombre, mismo orden)
        String newKey = storageService.saveProductImage(productoId, nueva, orden);

        // DB apunta al nuevo objectKey
        target.setUrl(newKey);
        productoRepository.save(p);
        em.flush();

        // borrar la vieja post-commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storageService.deleteObjects(List.of(oldKey));
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    // rollback => borramos la nueva subida para no dejar basura
                    storageService.deleteObjects(List.of(newKey));
                }
            }
        });

        return ProductoMapper.toDetalle(p, storageService);
    }

    // =========================
    //      HELPERS
    // =========================
    private void validarImagenes(List<MultipartFile> imagenes) {
        if (imagenes.size() > MAX_IMGS)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo " + MAX_IMGS + " imágenes");

        for (MultipartFile f : imagenes) {
            if (f == null || f.isEmpty()) continue;

            if (f.getSize() > MAX_SIZE)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagen supera 5MB");

            String ct = f.getContentType();
            if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tipo de imagen no permitido (solo jpg/png/webp)");
            }
        }
    }

    private String normalizarQ(String q) {
        if (q == null) return null;
        var t = q.trim();
        return t.isBlank() ? null : t;
    }

    private void validarPermisoProducto(Producto p, Long userId) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!user.esAdmin() && !p.getUsuario().getId().equals(userId)) {
            throw new ForbiddenException("No tenés permiso para editar este producto");
        }
    }

    private void validarUnaImagen(MultipartFile f) {
        if (f == null || f.isEmpty()) throw new BadRequestException("Imagen vacía");
        if (f.getSize() > MAX_SIZE) throw new BadRequestException("Imagen supera 5MB");
        String ct = f.getContentType();
        if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"))) {
            throw new BadRequestException("Tipo de imagen no permitido (solo jpg/png/webp)");
        }
    }

    private void reordenarSeguro(Producto p) {
        // 1) temporal
        int tmp = 1000;
        for (ProductoImagen img : p.getImagenes()) {
            img.setOrden(tmp++);
        }
        productoRepository.save(p);
        em.flush();

        // 2) final en el orden actual de la lista (ya viene por @OrderBy)
        int orden = 1;
        for (ProductoImagen img : p.getImagenes()) {
            img.setOrden(orden++);
        }
        productoRepository.save(p);
        em.flush();
    }

}
