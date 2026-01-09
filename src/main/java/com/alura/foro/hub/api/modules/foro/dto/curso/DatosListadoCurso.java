package com.alura.foro.hub.api.modules.foro.dto.curso;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Curso disponible en el sistema")
public record DatosListadoCurso(

        @Schema(example = "1")
        Long id,

        @Schema(example = "AWS")
        String nombre,

        @Schema(example = "2")
        Long categoriaId,

        @Schema(example = "Cloud")
        String categoriaNombre
) {}
