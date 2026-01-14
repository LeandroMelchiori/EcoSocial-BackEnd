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
import jakarta.persistence.EntityNotFoundException;
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

@Service
public class ProductoService {

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

        // 1) mover imágenes a trash (reversible)
        String opId = java.util.UUID.randomUUID().toString();
        String trashPrefix = storageService.moveProductDirToTrash(productoId, opId);

        // 2) registrar hooks: si commit -> purga; si rollback -> restaura
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // DB OK => borrado definitivo en MinIO
                storageService.purgeTrash(trashPrefix);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    // DB rollback => vuelvo todo atrás en MinIO
                    storageService.restoreTrashToProductDir(productoId, trashPrefix);
                }
            }
        });

        // 3) borrar en DB (si falla, salta exception => rollback => restore)
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
    ) {

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

        // actualizar campos simples
        p.setCategoria(categoria);
        p.setSubcategoria(subcategoria);
        p.setTitulo(dto.titulo().trim());
        p.setDescripcion(dto.descripcion().trim());

        // =========================
        //  REEMPLAZO DE IMÁGENES
        // =========================
        if (imagenes != null) {
            validarImagenes(imagenes);

            // Guardá las keys viejas ANTES de tocar DB
            List<String> keysViejas = p.getImagenes().stream()
                    .map(ProductoImagen::getUrl)
                    .toList();

            // 1) BORRADO DB (sí o sí antes de insertar por el unique)
            productoImagenRepository.deleteByProductoId(productoId);
            productoImagenRepository.flush(); // <- CLAVE para que se ejecute ya

            // 2) SUBIDA nuevas a MinIO + armado de nuevas filas
            int orden = 1;
            for (MultipartFile f : imagenes) {
                if (f == null || f.isEmpty()) continue;

                String key = storageService.saveProductImage(productoId, f, orden);

                ProductoImagen img = new ProductoImagen();
                img.setProducto(p);
                img.setOrden(orden);
                img.setUrl(key);

                p.getImagenes().add(img);
                orden++;
            }
             
            storageService.deleteObjects(keysViejas);
        }

        p = productoRepository.save(p);
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

}
