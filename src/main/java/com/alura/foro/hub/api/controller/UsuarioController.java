package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.dto.usuario.DatosUsuarioListado;
import com.alura.foro.hub.api.domain.dto.usuario.DatosUsuarioRegistro;
import com.alura.foro.hub.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Registrar usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuario registrado con exito")})
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody @Valid DatosUsuarioRegistro datos) {

        var usuario = usuarioService.registrar(datos);

        var dtoRespuesta = new DatosUsuarioListado(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getUsername()
        );
        return ResponseEntity.ok(dtoRespuesta);
    }

    @Operation(
            summary = "Convertir en Admin",
            description = "Permite a un administrador convertir otro usuario en ADMINISTRADOR"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol ADMINISTRADOR otorgado con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hacerAdmin(@PathVariable Long id) {
        usuarioService.asignarRolAdmin(id);
        return ResponseEntity.ok().build();
    }

}
