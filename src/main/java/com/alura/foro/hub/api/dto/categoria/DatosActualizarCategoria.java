package com.alura.foro.hub.api.dto.categoria;

import jakarta.validation.constraints.NotBlank;

public record DatosActualizarCategoria(
        @NotBlank String nombre
) {
}
