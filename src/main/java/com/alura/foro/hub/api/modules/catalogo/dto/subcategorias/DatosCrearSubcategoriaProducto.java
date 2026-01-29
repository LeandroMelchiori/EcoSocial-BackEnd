package com.alura.foro.hub.api.modules.catalogo.dto.subcategorias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "DatosCrearSubcategoriaProducto", description = "Payload para crear una subcategoría del catálogo.")
public record DatosCrearSubcategoriaProducto(
        @Schema(description = "ID de la categoría padre", example = "1")
        @NotNull Long categoriaId,

        @Schema(description = "Nombre de la subcategoría", example = "Panificados")
        @NotBlank @Size(max = 80) String nombre
) {}