package com.alura.foro.hub.api.modules.foro.dto.respuesta;

import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosListadoRespuestaHija;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalle de una respuesta con sus respuestas hijas")
public record DatosDetalleRespuesta(

        Long id,
        String mensaje,
        String autorNombre,
        Boolean solucion,
        LocalDateTime fechaCreacion,
        Long cantidadHijas,
        List<DatosListadoRespuestaHija> hijas
) {}
