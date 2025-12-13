package com.alura.foro.hub.api.domain.dto.respuesta;

import jakarta.validation.constraints.NotBlank;

public record DatosActualizarRespuesta(
        @NotBlank String mensaje
) {}

