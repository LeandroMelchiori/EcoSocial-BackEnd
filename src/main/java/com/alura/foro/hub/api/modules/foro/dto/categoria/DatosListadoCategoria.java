package com.alura.foro.hub.api.modules.foro.dto.categoria;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria de cursos")
public record DatosListadoCategoria(

        @Schema(example = "2")
        Long id,

        @Schema(example = "Cloud")
        String nombre
) {}
