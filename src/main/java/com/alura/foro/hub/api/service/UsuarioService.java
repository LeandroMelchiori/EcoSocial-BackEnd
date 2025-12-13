package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.domain.dto.usuario.DatosUsuarioRegistro;
import com.alura.foro.hub.api.repository.PerfilRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(DatosUsuarioRegistro datos) {

        if (usuarioRepository.existsByEmail(datos.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (usuarioRepository.existsByUsername(datos.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        var usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setEmail(datos.email());
        usuario.setUsername(datos.username());
        usuario.setPassword(passwordEncoder.encode(datos.password()));

        var rolUser = perfilRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("No existe el perfil ROLE_USER"));

        usuario.setPerfiles(List.of(rolUser));

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void asignarRolAdmin(Long usuarioId) {
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var rolAdmin = perfilRepository.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no existe"));

        if (!usuario.getPerfiles().contains(rolAdmin)) {
            usuario.getPerfiles().add(rolAdmin);
        }
    }

}
