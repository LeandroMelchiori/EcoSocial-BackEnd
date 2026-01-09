package com.alura.foro.hub.api.modules.foro.dto.topico;

import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar un tópico (campos opcionales)")
public record DatosActualizarTopico(
        @NotBlank
        @Schema(example = "Nuevo título")
        String titulo,

        @NotBlank
        @Schema(example = "Nuevo mensaje")
        String mensaje,

        @Schema(example = "1")
        Long cursoId,

        @Schema(example = "ACTIVO")
        StatusTopico status
) {}


