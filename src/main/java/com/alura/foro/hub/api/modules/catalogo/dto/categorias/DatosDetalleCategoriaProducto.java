package com.alura.foro.hub.api.modules.catalogo.dto.categorias;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DatosDetalleCategoriaProducto", description = "Detalle de categoría (admin y público).")
public record DatosDetalleCategoriaProducto(
        @Schema(example = "1") Long id,
        @Schema(example = "Alimentos") String nombre,
        @Schema(description = "Indica si la categoría está activa", example = "true") Boolean activo
) {}