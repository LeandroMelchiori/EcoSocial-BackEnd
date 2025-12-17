package com.alura.foro.hub.api.dto.usuario;

public record DatosUsuarioListado(
        Long id,
        String nombre,
        String email,
        String username
) {
    }
