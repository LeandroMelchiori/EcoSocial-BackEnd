package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;

import java.time.LocalDateTime;

public record DatosListadoTopico(
        Long id,
        String titulo,
        LocalDateTime fechaCreacion,
        String nombreAutor,
        Long cursoId,
        String nombreCurso,
        Long categoriaId,
        String nombreCategoria,
        StatusTopico status,
        Long cantidadRespuestas,
        LocalDateTime fechaUltimaRespuesta
) {}

