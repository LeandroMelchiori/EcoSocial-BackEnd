package com.alura.foro.hub.api.dto.respuestaHija;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta hija asociada a una respuesta")
public record DatosListadoRespuestaHija(
        @Schema(example = "10")
        Long id,

        @Schema(example = "Estoy de acuerdo, sumo un detalle...")
        String mensaje,

        @Schema(example = "Otro Usuario")
        String autorNombre,

        @Schema(example = "2025-12-18T19:10:00")
        LocalDateTime fechaCreacion,

        @Schema(example = "false")
        Boolean editado
) {}
