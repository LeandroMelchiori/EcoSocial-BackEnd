package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;

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

    public static DatosDetalleTopico toDetalle(Topico t) {
        return toDetalle(t, List.of());
    }
}