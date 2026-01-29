package com.alura.foro.hub.api.modules.catalogo.dto.subcategorias;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DatosDetalleSubcategoriaProducto", description = "Detalle de subcategoría (admin y público).")
public record DatosDetalleSubcategoriaProducto(
        @Schema(example = "10") Long id,
        @Schema(example = "1") Long categoriaId,
        @Schema(example = "Panificados") String nombre,
        @Schema(description = "Indica si la subcategoría está activa", example = "true") Boolean activo
) {}