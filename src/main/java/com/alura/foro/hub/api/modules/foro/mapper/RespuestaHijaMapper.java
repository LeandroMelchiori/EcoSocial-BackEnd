package com.alura.foro.hub.api.modules.foro.mapper;

import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import com.alura.foro.hub.api.modules.foro.domain.model.RespuestaHija;
import com.alura.foro.hub.api.user.domain.Usuario;

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
                rh.getFechaCreacion(),
                rh.getEditado()
        );
    }
}
