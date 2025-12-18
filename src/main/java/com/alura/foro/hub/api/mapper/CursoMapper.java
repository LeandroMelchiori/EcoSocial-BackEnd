package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;

public class CursoMapper {

    private CursoMapper() {
        // evitar instanciación
    }

    public static DatosListadoCurso toListado(Curso c) {
        return new DatosListadoCurso(
                c.getId(),
                c.getNombre(),
                c.getCategoria().getId(),
                c.getCategoria().getNombre()
        );
    }
}
