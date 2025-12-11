package com.alura.foro.hub.api.domain;

public record DatosRegistroTopico(
        String titulo,
        String mensaje,
        Long autorId,
        Long cursoId,
        StatusTopico estado
) {

}