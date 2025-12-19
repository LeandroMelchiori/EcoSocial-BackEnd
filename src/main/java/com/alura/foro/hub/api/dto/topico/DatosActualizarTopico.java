package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar un tópico (campos opcionales)")
public record DatosActualizarTopico(
        @Schema(example = "Nuevo título")
        String titulo,

        @Schema(example = "Nuevo mensaje")
        String mensaje,

        @Schema(example = "1")
        Long cursoId,

        @Schema(example = "ABIERTO")
        StatusTopico status
) {}


