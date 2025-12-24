package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.entity.model.Usuario;

import java.util.List;

public class TopicoMapper {
    private TopicoMapper() {}

    public static DatosDetalleTopico toDetalle(Topico t, List<DatosListadoRespuesta> respuestas) {
        return new DatosDetalleTopico(
                t.getId(),
                t.getTitulo(),
                t.getMensaje(),
                t.getFechaCreacion(),
                t.getAutor().getNombre(),
                t.getCurso().getId(),
                t.getCurso().getNombre(),
                t.getCurso().getCategoria().getId(),
                t.getCurso().getCategoria().getNombre(),
                t.getStatus(),
                respuestas
        );
    }

    public static Topico fromCrear(DatosRegistroTopico datos, Usuario autor, Curso curso) {
        return new Topico(datos, autor, curso);
    }

    public static void aplicarActualizacion(Topico t, DatosActualizarTopico datos, Curso curso) {
        if (datos.titulo() != null && !datos.titulo().isBlank()) {
            t.setTitulo(datos.titulo());
        }
        if (datos.mensaje() != null && !datos.mensaje().isBlank()) {
            t.setMensaje(datos.mensaje());
        }
        if (curso != null) {
            t.setCurso(curso);
        }
        if (datos.status() != null) {
            t.setStatus(datos.status());
        }
    }

    public static DatosDetalleTopico toDetalle(Topico t) {
        return toDetalle(t, List.of());
    }
}