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
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("No autenticado");
        }

        Object principal = auth.getPrincipal();

        // 1) Caso ideal: tu entidad como principal
        if (principal instanceof Usuario u) {
            return u.getId();
        }

        // 2) Caso típico en tests / inMemory / etc: UserDetails
        if (principal instanceof UserDetails ud) {
            return resolveUserIdByIdentificador(ud.getUsername());
        }

        // 3) Caso: principal String (a veces pasa)
        if (principal instanceof String s) {
            if ("anonymousUser".equalsIgnoreCase(s)) {
                throw new ForbiddenException("No autenticado");
            }
            return resolveUserIdByIdentificador(s);
        }

        // 4) Fallback final
        return resolveUserIdByIdentificador(auth.getName());
    }

    private Long resolveUserIdByIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        // Si algún filtro te setea auth.getName() como ID numérico
        try {
            return Long.valueOf(identificador);
        } catch (NumberFormatException ignored) {
            // Si no es número: es email o dni (igual que tu AuthenticationService)
            return identificador.contains("@")
                    ? usuarioRepository.findByEmail(identificador)
                    .map(Usuario::getId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"))
                    : usuarioRepository.findByDni(identificador)
                    .map(Usuario::getId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        }
    }
}
