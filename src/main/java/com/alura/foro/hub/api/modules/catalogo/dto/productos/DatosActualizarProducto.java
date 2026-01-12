package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatosActualizarProducto(
        @NotNull Long categoriaCatalogoId,
        Long subCategoriaCatalogoId,
        @NotBlank @Size(max = 120) String titulo,
        @NotBlank String descripcion
) {}
