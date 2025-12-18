// domain/dto/DatosListadoRespuesta.java
package com.alura.foro.hub.api.dto.respuesta;

import java.time.LocalDateTime;

public record DatosListadoRespuesta(
        Long id,
        String mensaje,
        String autorNombre,
        Boolean solucion,
        LocalDateTime fechaCreacion
) {}
