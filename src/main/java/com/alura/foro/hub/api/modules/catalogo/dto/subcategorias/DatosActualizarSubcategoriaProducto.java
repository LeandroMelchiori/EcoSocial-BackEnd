package com.alura.foro.hub.api.modules.catalogo.dto.subcategorias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "DatosActualizarSubcategoriaProducto", description = "Payload para actualizar una subcategoría del catálogo.")
public record DatosActualizarSubcategoriaProducto(
        @Schema(description = "ID de la categoría padre (permite mover la subcategoría)", example = "1")
        @NotNull Long categoriaId,

        @Schema(description = "Nombre actualizado de la subcategoría", example = "Panificados artesanales")
        @NotBlank @Size(max = 80) String nombre
) {}