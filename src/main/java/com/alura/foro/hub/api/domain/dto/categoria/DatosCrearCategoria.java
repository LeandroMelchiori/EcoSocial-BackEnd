package com.alura.foro.hub.api.domain.dto.categoria;

import jakarta.validation.constraints.NotBlank;

public record DatosCrearCategoria(
        @NotBlank String nombre
) {

}
