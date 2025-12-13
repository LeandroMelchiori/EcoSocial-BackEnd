package com.alura.foro.hub.api.domain.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosUsuarioRegistro(
        @NotBlank @Size(min = 3, max = 50) String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 4) String password,
        @NotBlank String username
) {}
