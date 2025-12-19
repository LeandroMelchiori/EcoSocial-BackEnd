package com.alura.foro.hub.api.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar una categoria")
public record DatosActualizarCategoria(

        @NotBlank
        @Schema(example = "Cloud")
        String nombre
) {}

