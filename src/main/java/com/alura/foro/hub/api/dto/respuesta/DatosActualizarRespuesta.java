package com.alura.foro.hub.api.dto.respuesta;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar una respuesta")
public record DatosActualizarRespuesta(
        @Schema(
                description = "Nuevo contenido de la respuesta",
                example = "Actualicé la respuesta con más detalle"
        )
        @NotBlank
        String mensaje
) {}
