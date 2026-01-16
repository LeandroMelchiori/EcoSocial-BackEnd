package com.alura.foro.hub.api.modules.foro.dto.respuestaHija;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar una respuesta hija")
public record DatosActualizarRespuestaHija(
        @Schema(description = "Nuevo contenido de la respuesta hija", example = "Actualicé la respuesta hija con más detalle")
        @NotBlank
        String mensaje
) {}
