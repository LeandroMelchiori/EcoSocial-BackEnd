package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.DatosUsuarioListado;
import com.alura.foro.hub.api.domain.DatosUsuarioRegistro;
import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class RegistroUsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    public ResponseEntity<?> registrar(@RequestBody @Valid DatosUsuarioRegistro datos) {

        // Validar email único
        if (usuarioRepository.existsByEmail(datos.email())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        // Validar username único
        if (usuarioRepository.existsByUsername(datos.username())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
        }

        // Crear entidad Usuario
        var usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setEmail(datos.email());
        usuario.setUsername(datos.username());
        usuario.setPassword(passwordEncoder.encode(datos.password()));

        usuarioRepository.save(usuario);

        // Devolver DTO limpio (sin password)
        var dtoRespuesta = new DatosUsuarioListado(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getUsername()
        );

        return ResponseEntity.ok(dtoRespuesta);
    }
}
