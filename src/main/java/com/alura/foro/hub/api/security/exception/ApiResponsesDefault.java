package com.alura.foro.hub.api.security.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 400,
                          "error": "Bad request",
                          "message": "Datos inválidos",
                          "path": "/topicos",
                          "fieldErrors": [
                            { "field": "titulo", "message": "no debe estar vacío" }
                          ]
                        }
                        """)
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Token inválido o ausente",
                          "path": "/topicos",
                          "fieldErrors": null
                        }
                        """)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "No autorizado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "No tenés permisos para esta acción",
                          "path": "/topicos/10",
                          "fieldErrors": null
                        }
                        """)
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "No encontrado",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Recurso no encontrado",
                          "path": "/topicos/999",
                          "fieldErrors": null
                        }
                        """)
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Conflicto de negocio",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 409,
                          "error": "Conflict",
                          "message": "El email ya está registrado",
                          "path": "/usuarios",
                          "fieldErrors": null
                        }
                        """)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(value = """
                        {
                          "timestamp": "2025-12-18T18:07:12.072114-03:00",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "Ocurrió un error inesperado",
                          "path": "/topicos",
                          "fieldErrors": null
                        }
                        """)
                )
        )
})
public @interface ApiResponsesDefault {
}
