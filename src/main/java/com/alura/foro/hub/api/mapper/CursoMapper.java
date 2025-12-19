package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.dto.curso.*;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.entity.model.Categoria;

public class CursoMapper {

    private CursoMapper() {}

    public static Curso fromCrear(DatosCrearCurso datos, Categoria categoria) {
        var c = new Curso();
        c.setNombre(datos.nombre().trim());
        c.setCategoria(categoria);
        return c;
    }

    public static void aplicarActualizacion(Curso c, DatosActualizarCurso datos, Categoria categoria) {
        c.setNombre(datos.nombre().trim());
        c.setCategoria(categoria);
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

