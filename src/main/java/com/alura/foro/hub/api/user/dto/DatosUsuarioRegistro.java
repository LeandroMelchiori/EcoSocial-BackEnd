package com.alura.foro.hub.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosUsuarioRegistro(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String dni,
        @Email @NotBlank String email,
        @NotBlank String password
) {}

