package com.alura.foro.hub.api.domain;

public record DatosActualizarTopico(
        String titulo,
        String mensaje,
        StatusTopico status
) {}


