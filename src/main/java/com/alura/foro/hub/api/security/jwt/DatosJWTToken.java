package com.alura.foro.hub.api.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT generado al autenticarse correctamente")
public record DatosJWTToken(
        @Schema(
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJmb3JvLWh1YiIsInN1YiI6InNhY2hhQG1haWwuY29tIiwiZXhwIjoxNzM0NTY3ODAwfQ.qwerty123"
        )
        String token
) {}