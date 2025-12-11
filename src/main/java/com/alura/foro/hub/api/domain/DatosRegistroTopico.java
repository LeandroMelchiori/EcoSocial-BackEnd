package com.alura.foro.hub.api.domain;

public record DatosRegistroTopico(
        String titulo,
        String mensaje,
        Long cursoId
) {

}