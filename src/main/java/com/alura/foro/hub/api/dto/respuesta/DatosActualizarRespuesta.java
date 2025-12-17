package com.alura.foro.hub.api.dto.respuesta;

import jakarta.validation.constraints.NotBlank;

public record DatosActualizarRespuesta(
        @NotBlank String mensaje
) {}

