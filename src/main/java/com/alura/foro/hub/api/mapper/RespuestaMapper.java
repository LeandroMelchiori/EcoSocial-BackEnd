package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosDetalleRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;

import java.util.List;

public class RespuestaMapper {

    private RespuestaMapper() {}

    public static Respuesta fromCrear(DatosCrearRespuesta dto, Topico topico, Usuario autor) {
        var r = new Respuesta();
        r.setMensaje(dto.mensaje().trim());
        r.setTopico(topico);
        r.setAutor(autor);
        r.setSolucion(false);
        return r;
    }

    public static void aplicarActualizacion(Respuesta r, DatosActualizarRespuesta dto) {
        r.setMensaje(dto.mensaje().trim());
    }

    public static DatosListadoRespuesta toListado(Respuesta r, Long cantidadHijas) {
        return new DatosListadoRespuesta(
                r.getId(),
                r.getMensaje(),
                r.getAutor().getNombre(),
                r.getSolucion(),
                r.getFechaCreacion(),
                cantidadHijas
        );
    }

    public static DatosDetalleRespuesta toDetalle(
            Respuesta r,
            Long cantidadHijas,
            List<DatosListadoRespuestaHija> hijas
    ) {
        return new DatosDetalleRespuesta(
                r.getId(),
                r.getMensaje(),
                r.getAutor().getNombre(),
                r.getSolucion(),
                r.getFechaCreacion(),
                cantidadHijas,
                hijas
        );
    }

}
