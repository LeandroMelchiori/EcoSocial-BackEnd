package com.alura.foro.hub.api.domain;

import java.time.LocalDateTime;

public record DatosDetalleTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String autorNombre,
        String cursoNombre,
        EstadoTopico estado
) {}
