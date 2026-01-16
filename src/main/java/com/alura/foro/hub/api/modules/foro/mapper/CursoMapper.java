package com.alura.foro.hub.api.modules.foro.mapper;

import com.alura.foro.hub.api.modules.foro.dto.curso.*;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;

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

