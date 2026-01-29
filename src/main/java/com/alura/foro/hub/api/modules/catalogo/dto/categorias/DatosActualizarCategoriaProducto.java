package com.alura.foro.hub.api.modules.catalogo.dto.categorias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "DatosActualizarCategoriaProducto", description = "Payload para actualizar una categoría del catálogo.")
public record DatosActualizarCategoriaProducto(
        @Schema(description = "Nombre actualizado de la categoría", example = "Alimentos y bebidas")
        @NotBlank @Size(max = 80)
        String nombre
) {}