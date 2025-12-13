package com.alura.foro.hub.api.domain.dto.topico;

public record DatosRegistroTopico(
        String titulo,
        String mensaje,
        Long cursoId
) {

}