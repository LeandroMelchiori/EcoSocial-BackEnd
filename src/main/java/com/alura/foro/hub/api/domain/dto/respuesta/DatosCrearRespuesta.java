// domain/dto/DatosCrearRespuesta.java
package com.alura.foro.hub.api.domain.dto.respuesta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosCrearRespuesta(
        @NotNull Long topicoId,
        @NotBlank String mensaje
) {}
