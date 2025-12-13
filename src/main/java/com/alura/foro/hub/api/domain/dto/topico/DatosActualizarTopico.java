package com.alura.foro.hub.api.domain.dto.topico;

import com.alura.foro.hub.api.domain.StatusTopico;

public record DatosActualizarTopico(
        String titulo,
        String mensaje,
        Long cursoId,
        StatusTopico status
) {}


