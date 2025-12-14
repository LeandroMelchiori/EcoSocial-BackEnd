package com.alura.foro.hub.api.domain.dto.topico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatosRegistroTopico(
        @NotBlank
        @Size(max = 255, message = "El título no puede superar 255 caracteres")
        String titulo,
        @NotBlank(message = "El mensaje es obligatorio")
        String mensaje,
        @NotNull(message = "Seleccionar el curso es obligatorio")
        Long cursoId
) {

}