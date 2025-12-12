package com.alura.foro.hub.api.domain;

import java.time.LocalDateTime;

public record DatosListadoTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String nombreAutor,
        String nombreCurso,
        StatusTopico status,
        Long cantidadRespuestas,
        LocalDateTime fechaUltimaRespuesta
) {}

