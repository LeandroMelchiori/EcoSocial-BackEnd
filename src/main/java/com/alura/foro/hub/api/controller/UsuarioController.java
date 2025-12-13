package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.dto.usuario.DatosUsuarioListado;
import com.alura.foro.hub.api.domain.dto.usuario.DatosUsuarioRegistro;
import com.alura.foro.hub.api.service.UsuarioService;
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

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hacerAdmin(@PathVariable Long id) {
        usuarioService.asignarRolAdmin(id);
        return ResponseEntity.ok().build();
    }

}
