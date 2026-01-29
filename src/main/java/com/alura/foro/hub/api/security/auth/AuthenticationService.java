package com.alura.foro.hub.api.security.auth;

import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {

        var usuarioOpt =
                identificador.contains("@")
                        ? usuarioRepository.findByEmail(identificador)
                        : usuarioRepository.findByDni(identificador);

        return usuarioOpt.orElseThrow(() ->
                new UsernameNotFoundException("Usuario no encontrado"));
    }


}

