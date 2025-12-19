// infra/errors/ApiError.java
package com.alura.foro.hub.api.security.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "ApiError", description = "Formato estándar de errores de la API")
public record ApiError(
        OffsetDateTime timestamp,
        @Schema(example = "409")
        int status,
        @Schema(example = "Conflict")
        String error,
        @Schema(example = "El email ya está registrado")
        String message,
        @Schema(example = "/usuarios")
        String path,
        @Schema(description = "Detalle de errores de validación por campo. Puede ser null si no aplica.")
        List<FieldErrorItem> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path, List<FieldErrorItem> fieldErrors) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, fieldErrors);
    }


}
