package com.alura.foro.hub.api.modules.catalogo.dto.subcategorias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatosCrearSubcategoriaProducto(
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 80) String nombre
) {}

