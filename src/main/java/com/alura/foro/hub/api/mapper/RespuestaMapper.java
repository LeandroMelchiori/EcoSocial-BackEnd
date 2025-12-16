package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosListadoRespuesta;

public class RespuestaMapper {
    private RespuestaMapper() {}

    public static DatosListadoRespuesta toListado(Respuesta r) {
        return new DatosListadoRespuesta(
                r.getId(),
                r.getMensaje(),
                r.getAutor().getNombre(),
                r.getSolucion(),
                r.getFechaCreacion()
        );
    }
}
