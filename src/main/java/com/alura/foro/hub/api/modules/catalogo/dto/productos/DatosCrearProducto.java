package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos necesarios para crear un producto del catálogo")
public record DatosCrearProducto(
        @Schema(
                description = "ID de la categoría del catálogo",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long categoriaCatalogoId,

        @Schema(
                description = "ID de la subcategoría del catálogo",
                example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long subCategoriaCatalogoId,
        @Schema(
                description = "Título del producto",
                example = "Pan casero integral",
                maxLength = 120
        )
        @NotBlank
        @Size(min = 5, max = 120)
        String titulo,

        @Schema(
                description = "Descripción detallada del producto",
                example = "Pan artesanal elaborado con masa madre, sin conservantes.",
                maxLength = 3000
        )
        @NotBlank
        @Size(min = 10, max = 3000)
        String descripcion
) {}
