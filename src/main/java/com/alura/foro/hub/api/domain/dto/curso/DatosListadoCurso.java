package com.alura.foro.hub.api.domain.dto.curso;

public record DatosListadoCurso(
        Long id,
        String nombre,
        Long categoriaId,
        String categoriaNombre
) {}