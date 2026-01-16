package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatosCrearProducto(
        @NotNull Long categoriaCatalogoId,
        @NotNull Long subCategoriaCatalogoId,

        @NotBlank @Size(min = 5, max = 120) String titulo,
        @NotBlank @Size(min = 20, max = 3000) String descripcion
) {}
