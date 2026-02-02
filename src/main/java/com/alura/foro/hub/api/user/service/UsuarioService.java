package com.alura.foro.hub.api.user.service;

import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.DatosUsuarioRegistro;
import com.alura.foro.hub.api.user.repository.PerfilRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
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
        if (usuarioRepository.existsByDni(datos.dni())) {
            throw new BusinessException("El DNI ya está registrado");
        }

        var usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setApellido(datos.apellido());
        usuario.setDni(datos.dni());
        usuario.setEmail(datos.email());
        usuario.setPassword(passwordEncoder.encode(datos.password()));
        usuario.setActivo(true);

        var rolUser = perfilRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("No existe el perfil USER"));

        usuario.setPerfiles(new ArrayList<>(List.of(rolUser)));

        return usuarioRepository.save(usuario);
    }


    @Transactional
    public void asignarRolAdmin(Long usuarioId) {

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var rolAdmin = perfilRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("No existe el perfil ADMIN"));

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

        var rolAdmin = perfilRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("No existe el perfil ADMIN"));

        if (!usuario.getPerfiles().contains(rolAdmin)) {
            throw new BusinessException("El usuario no tiene el rol ADMIN");
        }

        long cantidadAdmins = usuarioRepository.countUsuariosConRol("ADMIN");

        if (cantidadAdmins <= 1) {
            throw new BusinessException("No se puede quitar el rol ADMIN al último administrador del sistema");
        }

        usuario.getPerfiles().remove(rolAdmin);
    }


}