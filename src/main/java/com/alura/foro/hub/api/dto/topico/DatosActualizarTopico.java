package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;

public record DatosActualizarTopico(
        String titulo,
        String mensaje,
        Long cursoId,
        StatusTopico status
) {}


