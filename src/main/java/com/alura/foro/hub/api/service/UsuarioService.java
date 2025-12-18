package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.dto.usuario.DatosUsuarioRegistro;
import com.alura.foro.hub.api.repository.PerfilRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
            throw new BusinessException("El email ya está registrado");
        }
        if (usuarioRepository.existsByUsername(datos.username())) {
            throw new BusinessException("El nombre de usuario ya está en uso");
        }

        var usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setEmail(datos.email());
        usuario.setUsername(datos.username());
        usuario.setPassword(passwordEncoder.encode(datos.password()));

        var rolUser = perfilRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("ROL_USER no existe"));

        usuario.setPerfiles(new ArrayList<>(List.of(rolUser)));

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void asignarRolAdmin(Long usuarioId) {

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var rolAdmin = perfilRepository.findByNombre("admin") // o "ADMIN"
                .orElseThrow(() -> new IllegalStateException("No existe el perfil admin"));

        if (!usuario.getPerfiles().contains(rolAdmin)) {
            usuario.getPerfiles().add(rolAdmin);
        }
    }

    @Transactional
    public void quitarRolAdmin(Long usuarioObjetivoId, Long adminEjecutorId) {

        if (usuarioObjetivoId.equals(adminEjecutorId)) {
            throw new BusinessException("No podés quitarte tu propio rol de administrador");
        }

        var usuario = usuarioRepository.findById(usuarioObjetivoId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var rolAdmin = perfilRepository.findByNombre("admin")
                .orElseThrow(() -> new IllegalStateException("No existe el perfil admin"));

        if (!usuario.getPerfiles().contains(rolAdmin)) {
            throw new BusinessException("El usuario no tiene el rol admin");
        }

        long cantidadAdmins = usuarioRepository.countUsuariosConRol("admin");

        if (cantidadAdmins <= 1) {
            throw new BusinessException("No se puede quitar el rol admin al último administrador del sistema");
        }

        usuario.getPerfiles().remove(rolAdmin);
    }

}
