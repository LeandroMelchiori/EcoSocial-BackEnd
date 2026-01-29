package com.alura.foro.hub.api.user.controller;

import com.alura.foro.hub.api.user.dto.DatosUsuarioListado;
import com.alura.foro.hub.api.user.dto.DatosUsuarioRegistro;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.user.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Crea un usuario nuevo y devuelve sus datos básicos"
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosUsuarioListado.class),
                    examples = @ExampleObject(
                            name = "Registro OK",
                            value = """
                        {
                          "id": 1,
                          "nombre": "user",
                          "email": "users@gmail.com",
                          "username": "user"
                        }
                        """
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = com.alura.foro.hub.api.security.exception.ApiError.class),
                    examples = @ExampleObject(
                            name = "Email duplicado",
                            value = """
                        {
                          "timestamp": "2025-12-18T19:30:00-03:00",
                          "status": 409,
                          "error": "Conflict",
                          "message": "El email ya está registrado",
                          "path": "/usuarios",
                          "fieldErrors": null
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosUsuarioRegistro.class),
                    examples = @ExampleObject(
                            name = "Registro",
                            value = """
                        {
                          "nombre": "user",
                          "email": "users@gmail.com",
                          "password": "123456",
                          "username": "user"
                        }
                        """
                    )
            )
    )
    @PostMapping
    public ResponseEntity<DatosUsuarioListado> registrar(
            @RequestBody @Valid DatosUsuarioRegistro datos) {

        var usuario = usuarioService.registrar(datos);

        var dtoRespuesta = new DatosUsuarioListado(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getUsername()
        );

        return ResponseEntity.status(201).body(dtoRespuesta);
    }

    @Operation(
            summary = "Convertir en Admin",
            description = "Permite a un administrador convertir otro usuario en ADMINISTRADOR"
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Rol administrador otorgado con éxito"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado: se requieren privilegios de administrador",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = com.alura.foro.hub.api.security.exception.ApiError.class
                    ),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Forbidden",
                            value = """
                        {
                          "timestamp": "2025-12-18T19:40:00-03:00",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "No tenés permisos para realizar esta acción",
                          "path": "/usuarios/5/admin",
                          "fieldErrors": null
                        }
                        """
                    )
            )
    )
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hacerAdmin(@PathVariable Long id) {
        usuarioService.asignarRolAdmin(id);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Quitar privilegio de Admin",
            description = "Permite a un administrador quitar el rol ADMIN a otro usuario"
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "204",
            description = "Rol administrador quitado con éxito"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado: se requieren privilegios de administrador",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = com.alura.foro.hub.api.security.exception.ApiError.class
                    ),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Forbidden",
                            value = """
                        {
                          "timestamp": "2025-12-18T19:40:00-03:00",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "No tenés permisos para realizar esta acción",
                          "path": "/usuarios/5/admin",
                          "fieldErrors": null
                        }
                        """
                    )
            )
    )
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> quitarAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        usuarioService.quitarRolAdmin(id, adminLogueado.getId());
        return ResponseEntity.noContent().build();
    }




}
