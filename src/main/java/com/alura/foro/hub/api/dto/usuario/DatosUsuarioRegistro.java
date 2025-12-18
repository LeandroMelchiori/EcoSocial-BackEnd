package com.alura.foro.hub.api.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosUsuarioRegistro(

        @NotBlank @Size(min = 3, max = 50)
        @Schema(description = "Nombre", example = "user")
        String nombre,
        @NotBlank @Email
        @Schema(description = "Email de usuario", example = "users@gmail.com")
        String email,
        @NotBlank @Size(min = 4)
        @Schema(description = "Contraseña", example = "123456")
        String password,
        @NotBlank
        @Schema(description = "Nombre de usuario", example = "user")
        String username
) {}
