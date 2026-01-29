package com.alura.foro.hub.api.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para autenticación")
public record UsuarioAuthenticateData(
        @NotBlank String identificador, // email o dni
        @NotBlank String password
) {}
