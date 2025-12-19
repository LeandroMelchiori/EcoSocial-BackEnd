package com.alura.foro.hub.api.dto.curso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosCrearCurso(
        @NotBlank
        @Schema(description = "Nombre del curso", example = "IAM")
        String nombre,
        @NotNull
        @Schema(description = "ID de la categoria", example = "1")
        Long categoriaId

) {
}
