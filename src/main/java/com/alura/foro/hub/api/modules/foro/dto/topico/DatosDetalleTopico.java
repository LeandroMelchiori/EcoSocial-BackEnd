package com.alura.foro.hub.api.modules.foro.dto.topico;

import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosListadoRespuesta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

// descripcion:
@Schema(description = "Detalle completo de un tópico con sus respuestas")
public record DatosDetalleTopico(

        @Schema(example = "10")
        Long id,

        @Schema(example = "Consulta sobre monotributo")
        String titulo,

        @Schema(example = "¿Cómo facturo si vendo por Instagram?")
        String mensaje,

        @Schema(example = "2025-12-18T18:30:00")
        LocalDateTime fechaCreacion,

        @Schema(example = "Leandro")
        String autorNombre,

        @Schema(example = "1")
        Long cursoId,

        @Schema(example = "Emprendimientos digitales")
        String cursoNombre,

        @Schema(example = "2")
        Long categoriaId,

        @Schema(example = "Economía social")
        String categoriaNombre,

        @Schema(example = "ACTIVO")
        StatusTopico status,

        @Schema(example = "false")
        Boolean editado,

        List<DatosListadoRespuesta> respuestas
) { }
