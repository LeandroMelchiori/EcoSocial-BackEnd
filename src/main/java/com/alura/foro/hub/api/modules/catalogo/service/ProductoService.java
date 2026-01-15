package com.alura.foro.hub.api.modules.catalogo.service;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosActualizarProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosDetalleProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosListadoProducto;
import com.alura.foro.hub.api.modules.catalogo.mapper.ProductoMapper;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Usuario; // ajustá paquete
import com.alura.foro.hub.api.user.repository.UsuarioRepository; // ajustá paquete
import com.alura.foro.hub.api.modules.catalogo.domain.*;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosCrearProducto;
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
    private final StorageService storageService;
    private final ProductoImagenRepository productoImagenRepository;

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

        // keys actuales (por si querés borrar directo)
        // pero como movemos a trash, no las necesitamos
        String opId = java.util.UUID.randomUUID().toString();
        String trashPrefix = storageService.moveProductDirToTrash(productoId, opId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                storageService.purgeTrash(trashPrefix);
            }
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storageService.restoreTrashToProductDir(productoId, trashPrefix);
                }
            }
        });

        // IMPORTANTE: si Producto tiene orphanRemoval para imagenes,
        // al borrar producto se borran las filas de producto_imagenes solas.
        productoRepository.delete(p);
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
        if (imagenes == null) {
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

}
