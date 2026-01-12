package com.alura.foro.hub.api.modules.catalogo.dto.categorias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosActualizarCategoriaProducto(
        @NotBlank @Size(max = 80) String nombre
) {}
