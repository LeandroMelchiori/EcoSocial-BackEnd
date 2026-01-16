package com.alura.foro.hub.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos básicos de un usuario")
public record DatosUsuarioListado(
        @Schema(example = "1") Long id,
        @Schema(example = "user") String nombre,
        @Schema(example = "users@gmail.com") String email,
        @Schema(example = "user") String username
) {}
