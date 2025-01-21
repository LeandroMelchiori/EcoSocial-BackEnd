package com.alura.foro.hub.api.domain;

public record DatosUsuarioListado(
        Long id,
        String nombre,
        String email,
        String username
) {
    }
