package com.alura.foro.hub.api.domain;

import jakarta.validation.constraints.NotBlank;

public record DatosActualizarRespuesta(
        @NotBlank String mensaje
) {}

