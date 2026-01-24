package com.alura.foro.hub.api.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para autenticación")
public record UsuarioAuthenticateData(

        @NotBlank
        @Schema(example = "sacha@mail.com", description = "Email del usuario")
        String username,

        @NotBlank
        @Schema(example = "123456example", description = "Contraseña del usuario")
        String password
) {}