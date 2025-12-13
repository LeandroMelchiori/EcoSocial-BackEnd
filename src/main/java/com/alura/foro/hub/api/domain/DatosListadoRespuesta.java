// domain/dto/DatosListadoRespuesta.java
package com.alura.foro.hub.api.domain;

import java.time.LocalDateTime;

public record DatosListadoRespuesta(
        Long id,
        Long topicoId,
        String mensaje,
        LocalDateTime fechaCreacion,
        Long autorId,
        String autorNombre,
        Boolean solucion
) {}
