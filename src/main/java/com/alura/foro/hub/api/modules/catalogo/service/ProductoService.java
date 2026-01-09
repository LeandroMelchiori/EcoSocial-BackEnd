package com.alura.foro.hub.api.modules.catalogo.service;

import com.alura.foro.hub.api.user.domain.Usuario; // ajustá paquete
import com.alura.foro.hub.api.user.repository.UsuarioRepository; // ajustá paquete
import com.alura.foro.hub.api.modules.catalogo.domain.*;
import com.alura.foro.hub.api.modules.catalogo.dto.DatosCrearProducto;
import com.alura.foro.hub.api.modules.catalogo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaCatalogoRepository categoriaCatalogoRepository,
                           SubCategoriaCatalogoRepository subCategoriaCatalogoRepository,
                           UsuarioRepository usuarioRepository,
                           StorageService storageService) {
        this.productoRepository = productoRepository;
        this.categoriaCatalogoRepository = categoriaCatalogoRepository;
        this.subCategoriaCatalogoRepository = subCategoriaCatalogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.storageService = storageService;
    }

    @Transactional
    public Producto crear(DatosCrearProducto dto, List<MultipartFile> imagenes, Long userId) throws IOException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findById(dto.categoriaCatalogoId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        Subcategoria subcategoria = null;
        if (dto.subCategoriaCatalogoId() != null) {
            subcategoria = subCategoriaCatalogoRepository.findById(dto.subCategoriaCatalogoId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));

            // opcional: validar que la subcategoria pertenezca a la categoria
            if (!subcategoria.getCategoria().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException("La subcategoría no pertenece a la categoría indicada");
            }
        }

        if (imagenes != null) {
            if (imagenes.size() > MAX_IMGS) throw new IllegalArgumentException("Máximo " + MAX_IMGS + " imágenes");
            for (MultipartFile f : imagenes) {
                if (f.isEmpty()) continue;
                if (f.getSize() > MAX_SIZE) throw new IllegalArgumentException("Imagen supera 5MB");
                String ct = f.getContentType();
                if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"))) {
                    throw new IllegalArgumentException("Tipo de imagen no permitido (solo jpg/png/webp)");
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

        // Guardamos primero el producto para tener ID
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
        }

        // Cascade ALL: guarda imágenes también
        return productoRepository.save(p);
    }
}
