package com.alura.foro.hub.api.security.auth;

import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long userId(Authentication auth) {
        if (auth == null) throw new ForbiddenException("No autenticado");

        Object principal = auth.getPrincipal();

        // Caso PROD (tu filtro mete Usuario)
        if (principal instanceof Usuario u) return u.getId();

        // Caso TEST / otros providers (UserDetails / String)
        String username;
        if (principal instanceof UserDetails ud) username = ud.getUsername();
        else username = auth.getName();

        return usuarioRepository.findByUsername(username)
                .map(Usuario::getId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}
