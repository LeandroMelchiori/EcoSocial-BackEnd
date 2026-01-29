package com.alura.foro.hub.api.modules.catalogo.mapper;

import com.alura.foro.hub.api.modules.catalogo.domain.Producto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosDetalleProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosImagenProducto;
import com.alura.foro.hub.api.modules.catalogo.service.MinioStorageService;
import com.alura.foro.hub.api.modules.catalogo.service.StorageService;

public class ProductoMapper {

    public static DatosDetalleProducto toDetalle(Producto p, StorageService storage) {
        var imgs = p.getImagenes().stream()
                .map(img -> new DatosImagenProducto(
                        img.getId(),
                        img.getOrden(),
                        storage.getUrl(img.getUrl()) // url presignada
                ))
                .toList();

        return new DatosDetalleProducto(
                p.getId(),
                p.getUsuario() != null ? p.getUsuario().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getSubcategoria() != null ? p.getSubcategoria().getId() : null,
                p.getTitulo(),
                p.getDescripcion(),
                p.getActivo(),
                p.getFechaCreacion(),
                imgs
        );
    }

}
