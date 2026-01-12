package com.alura.foro.hub.api.modules.catalogo.mapper;

import com.alura.foro.hub.api.modules.catalogo.domain.Producto;
import com.alura.foro.hub.api.modules.catalogo.domain.ProductoImagen;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosDetalleProducto;

public class ProductoMapper {

    public static DatosDetalleProducto toDetalle(Producto p) {
        var urls = p.getImagenes().stream().map(ProductoImagen::getUrl).toList();
        return new DatosDetalleProducto(
                p.getId(),
                p.getUsuario() != null ? p.getUsuario().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getSubcategoria() != null ? p.getSubcategoria().getId() : null,
                p.getTitulo(),
                p.getDescripcion(),
                p.getActivo(),
                p.getFechaCreacion(),
                urls
        );
    }

}
