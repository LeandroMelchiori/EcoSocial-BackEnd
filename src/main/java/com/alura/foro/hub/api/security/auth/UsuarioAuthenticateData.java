package com.alura.foro.hub.api.security.auth;

public record UsuarioAuthenticateData(
        String username,
        String password) {
}
