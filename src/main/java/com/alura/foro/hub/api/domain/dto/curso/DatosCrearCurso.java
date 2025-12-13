package com.alura.foro.hub.api.domain.dto.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosCrearCurso(
        @NotBlank String nombre,
        @NotNull Long categoriaId

) {
}
