package com.alura.foro.hub.api.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DatosCrearCategoria(
        @Schema(description = "Nombre de la categoria", example = "AWS")
        @NotBlank String nombre
) {

}
