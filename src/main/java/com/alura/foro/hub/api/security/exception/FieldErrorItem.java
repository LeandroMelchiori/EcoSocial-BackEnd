package com.alura.foro.hub.api.security.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FieldErrorItem", description = "Error de validación asociado a un campo")
public record FieldErrorItem(
        @Schema(example = "titulo")
        String field,

        @Schema(example = "no debe estar vacío")
        String message
) {}
