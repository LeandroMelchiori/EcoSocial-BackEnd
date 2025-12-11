package com.alura.foro.hub.api.domain;

import java.time.LocalDateTime;

public record DatosListadoTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String nombreAutor,
        String nombreCurso,
        StatusTopico status
) {
    // Constructor auxiliar para poder usar DatosListadoTopico::new
    public DatosListadoTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor().getNombre(),
                topico.getCurso().getNombre(),
                topico.getStatus()
        );
    }
}
