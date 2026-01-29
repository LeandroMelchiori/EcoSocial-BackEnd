package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalle completo de un producto del catálogo")
public record DatosDetalleProducto(

        @Schema(example = "10")
        Long id,

        @Schema(description = "ID del usuario creador", example = "3")
        Long usuarioId,

        @Schema(description = "ID de la categoría", example = "2")
        Long categoriaId,

        @Schema(description = "ID de la subcategoría", example = "5")
        Long subcategoriaId,

        @Schema(example = "Pan casero integral")
        String titulo,

        @Schema(
                example = "Pan artesanal elaborado con masa madre, sin conservantes."
        )
        String descripcion,

        @Schema(description = "Indica si el producto está activo", example = "true")
        Boolean activo,

        @Schema(
                description = "Fecha de creación",
                example = "2026-01-26T10:30:00"
        )
        LocalDateTime fechaCreacion,

        @Schema(description = "Imágenes asociadas al producto")
        List<DatosImagenProducto> imagenes
) {}
