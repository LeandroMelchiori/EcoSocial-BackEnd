// domain/dto/DatosListadoRespuesta.java
package com.alura.foro.hub.api.modules.foro.dto.respuesta;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Respuesta asociada a un tópico")
public record DatosListadoRespuesta(

        @Schema(example = "45")
        Long id,

        @Schema(example = "Tenés que inscribirte como monotributista")
        String mensaje,

        @Schema(example = "Otro Usuario")
        String autorNombre,

        @Schema(example = "false")
        Boolean solucion,

        @Schema(example = "2025-12-18T19:10:00")
        LocalDateTime fechaCreacion,

        @Schema(example = "8")
        Long cantidadHijas,

        @Schema(example = "false")
        Boolean editado
) {}
