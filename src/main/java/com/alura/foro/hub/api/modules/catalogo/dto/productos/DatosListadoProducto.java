package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Producto resumido para listados")
public record DatosListadoProducto(

        @Schema(example = "10")
        Long id,

        @Schema(example = "Pan casero integral")
        String titulo,

        @Schema(example = "true")
        Boolean activo,

        @Schema(example = "2026-01-26T10:30:00")
        LocalDateTime fechaCreacion,

        @Schema(example = "3")
        Long usuarioId,

        @Schema(example = "2")
        Long categoriaId,

        @Schema(example = "5")
        Long subcategoriaId,

        @Schema(
                description = "URL de la imagen principal",
                example = "https://cdn.midominio.com/productos/pan-thumb.jpg"
        )
        String thumbnailUrl
) {}

