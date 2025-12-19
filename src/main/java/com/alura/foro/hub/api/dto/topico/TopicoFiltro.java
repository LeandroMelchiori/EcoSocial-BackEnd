package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;

import java.time.LocalDateTime;

public record TopicoFiltro(
        String q,              // texto libre (título o mensaje)
        Long cursoId,           // o String cursoNombre, según tu modelo
        Long autorId,           // id del usuario autor
        StatusTopico status,    // enum
        LocalDateTime desde,    // fechaCreacion >= desde
        LocalDateTime hasta,     // fechaCreacion <= hasta
        String nombreCurso,
        String nombreCategoria
) {}

