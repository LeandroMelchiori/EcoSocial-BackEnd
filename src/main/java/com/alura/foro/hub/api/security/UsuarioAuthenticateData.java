package com.alura.foro.hub.api.security;

public record UsuarioAuthenticateData(
        String username,
        String password) {
}
