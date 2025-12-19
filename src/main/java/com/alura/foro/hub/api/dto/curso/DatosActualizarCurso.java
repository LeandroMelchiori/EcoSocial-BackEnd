package com.alura.foro.hub.api.dto.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar un curso")
public record DatosActualizarCurso(

        @NotBlank
        @Schema(example = "AWS Avanzado")
        String nombre,

        @NotNull
        @Schema(example = "2")
        Long categoriaId
) {}

