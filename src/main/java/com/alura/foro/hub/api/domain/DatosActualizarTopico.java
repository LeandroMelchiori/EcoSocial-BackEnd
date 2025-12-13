package com.alura.foro.hub.api.domain;

import jakarta.validation.constraints.NotNull;

public record DatosActualizarTopico(
        String titulo,
        String mensaje,
        Long cursoId,
        StatusTopico status
) {}


