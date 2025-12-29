package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.entity.model.RespuestaHija;
import com.alura.foro.hub.api.entity.model.Usuario;

public class RespuestaHijaMapper {

    private RespuestaHijaMapper() {}

    public static RespuestaHija fromCrear(DatosCrearRespuestaHija dto, Respuesta respuesta, Usuario autor) {
        var rh = new RespuestaHija();
        rh.setMensaje(dto.mensaje().trim());
        rh.setRespuesta(respuesta);
        rh.setAutor(autor);
        return rh;
    }

    public static DatosListadoRespuestaHija toListado(RespuestaHija rh) {
        return new DatosListadoRespuestaHija(
                rh.getId(),
                rh.getMensaje(),
                rh.getAutor().getNombre(),
                rh.getFechaCreacion()
        );
    }
}
