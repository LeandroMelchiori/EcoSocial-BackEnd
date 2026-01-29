package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para actualizar un producto existente")
public record DatosActualizarProducto(

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
                nullable = true
        )
        Long subCategoriaCatalogoId,

        @Schema(
                description = "Nuevo título del producto",
                example = "Pan casero integral (actualizado)",
                maxLength = 120
        )
        @NotBlank
        @Size(max = 120)
        String titulo,

        @Schema(
                description = "Nueva descripción del producto",
                example = "Ahora también disponible en versión con semillas."
        )
        @NotBlank
        String descripcion
) {}
