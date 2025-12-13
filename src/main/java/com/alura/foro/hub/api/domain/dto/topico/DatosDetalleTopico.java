package com.alura.foro.hub.api.domain.dto.topico;

import com.alura.foro.hub.api.domain.StatusTopico;
import com.alura.foro.hub.api.domain.Topico;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosListadoRespuesta;

import java.time.LocalDateTime;
import java.util.List;

// descripcion:
public record DatosDetalleTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String autorNombre,
        String cursoNombre,
        String categoriaNombre,
        StatusTopico status,
        List<DatosListadoRespuesta> respuestas
) {
    public DatosDetalleTopico(Topico topico) {
        this(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor().getNombre(),
                topico.getCurso().getNombre(),
                topico.getCurso().getCategoria().getNombre(),
                topico.getStatus(),
                List.of()
        );
    }
}