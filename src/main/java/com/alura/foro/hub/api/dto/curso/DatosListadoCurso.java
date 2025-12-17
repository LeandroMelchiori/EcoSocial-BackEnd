package com.alura.foro.hub.api.dto.curso;

public record DatosListadoCurso(
        Long id,
        String nombre,
        Long categoriaId,
        String categoriaNombre
) {}