package com.alura.foro.hub.api.modules.catalogo.dto.categorias;

public record DatosDetalleCategoriaProducto(
        Long id,
        String nombre,
        Boolean activo
) {}
