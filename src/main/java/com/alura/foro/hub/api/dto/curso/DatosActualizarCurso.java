package com.alura.foro.hub.api.dto.curso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosActualizarCurso(
        @NotBlank String nombre,
        @NotNull Long categoriaId

){

}
