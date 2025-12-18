// domain/dto/DatosCrearRespuesta.java
package com.alura.foro.hub.api.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosCrearRespuesta(
        @NotNull(message = "Topico no identificado")
        @Schema(description = "ID del topico", example = "1")
        Long topicoId,
        @NotBlank(message = "El mensaje es obligatorio")
        @Schema(description = "Mensaje de respuesta", example = "Respuesta de ejemplo")
        String mensaje
) {}