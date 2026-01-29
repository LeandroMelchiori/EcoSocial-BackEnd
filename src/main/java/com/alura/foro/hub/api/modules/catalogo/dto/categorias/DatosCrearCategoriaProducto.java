package com.alura.foro.hub.api.modules.catalogo.dto.categorias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "DatosCrearCategoriaProducto", description = "Payload para crear una categoría del catálogo.")
public record DatosCrearCategoriaProducto(
        @Schema(description = "Nombre de la categoría", example = "Alimentos")
        @NotBlank @Size(max = 80)
        String nombre
) {}

