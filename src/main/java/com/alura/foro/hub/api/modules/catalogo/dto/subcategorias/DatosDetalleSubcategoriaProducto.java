package com.alura.foro.hub.api.modules.catalogo.dto.subcategorias;

public record DatosDetalleSubcategoriaProducto(
        Long id,
        Long categoriaId,
        String nombre,
        Boolean activo
) {}
