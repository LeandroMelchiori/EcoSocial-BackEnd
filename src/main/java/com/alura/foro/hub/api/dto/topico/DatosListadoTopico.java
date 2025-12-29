package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Resumen de un tópico del foro. Incluye métricas y estado de edición."
)
public record DatosListadoTopico(

        @Schema(example = "15", description = "Identificador único del tópico")
        Long id,

        @Schema(example = "Problema con facturación en monotributo",
                description = "Título del tópico")
        String titulo,

        @Schema(example = "2025-12-18T18:45:00",
                description = "Fecha de creación del tópico")
        LocalDateTime fechaCreacion,

        @Schema(example = "Juan Pérez",
                description = "Nombre del autor del tópico")
        String nombreAutor,

        @Schema(example = "3",
                description = "Identificador del curso asociado")
        Long cursoId,

        @Schema(example = "Introducción a Java",
                description = "Nombre del curso asociado")
        String nombreCurso,

        @Schema(example = "2",
                description = "Identificador de la categoría del curso")
        Long categoriaId,

        @Schema(example = "Programación",
                description = "Nombre de la categoría del curso")
        String nombreCategoria,

        @Schema(example = "ACTIVO",
                description = "Estado actual del tópico (ACTIVO, SOLUCIONADO, CERRADO)")
        StatusTopico status,

        @Schema(example = "5",
                description = "Cantidad total de respuestas asociadas al tópico")
        Long cantidadRespuestas,

        @Schema(example = "2025-12-18T19:10:00",
                description = "Fecha de la última respuesta publicada en el tópico")
        LocalDateTime fechaUltimaRespuesta,

        @Schema(
                example = "false",
                description = """
                        Indica si el contenido original del tópico fue editado luego de su creación.
                        Si es true, el cliente puede mostrar la leyenda "mensaje editado".
                        """
        )
        Boolean editado
) {}

