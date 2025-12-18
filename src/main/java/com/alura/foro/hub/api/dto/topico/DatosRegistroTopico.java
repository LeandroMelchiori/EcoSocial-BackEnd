package com.alura.foro.hub.api.dto.topico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear un tópico")
public record DatosRegistroTopico(
        @NotBlank
        @Size(max = 255, message = "El título no puede superar 255 caracteres")
        @Schema(description = "Título del tópico", example = "Consulta sobre monotributo")
        String titulo,
        @NotBlank(message = "El mensaje es obligatorio")
        @Schema(description = "Mensaje detallado de la consulta", example = "¿Cómo facturo si vendo por Instagram?")
        String mensaje,
        @NotNull(message = "Seleccionar el curso es obligatorio")
        @Schema(description = "ID del curso relacionado", example = "1")
        Long cursoId
) { }