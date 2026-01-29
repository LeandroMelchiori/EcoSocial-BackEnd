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

        if (principal instanceof Usuario u) return u.getId();

        // fallback: si por algún motivo principal es String con el id
        String name = auth.getName();
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
    }
}
