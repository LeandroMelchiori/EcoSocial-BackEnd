package com.alura.foro.hub.api.dto.topico;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;

import java.time.LocalDateTime;
import java.util.List;

// descripcion:
public record DatosDetalleTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String autorNombre,
        Long cursoId,
        String cursoNombre,
        Long categoriaId,
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
                topico.getCurso().getId(),
                topico.getCurso().getNombre(),
                topico.getCurso().getCategoria().getId(),
                topico.getCurso().getCategoria().getNombre(),
                topico.getStatus(),
                List.of()
        );
    }

}