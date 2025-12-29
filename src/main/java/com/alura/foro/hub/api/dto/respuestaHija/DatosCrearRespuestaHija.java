package com.alura.foro.hub.api.dto.respuestaHija;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para crear una respuesta hija (respuesta a una respuesta)")
public record DatosCrearRespuestaHija(
        @NotBlank(message = "El mensaje es obligatorio")
        @Schema(description = "Mensaje de la respuesta hija", example = "Estoy de acuerdo, sumo un detalle...")
        String mensaje
) {}