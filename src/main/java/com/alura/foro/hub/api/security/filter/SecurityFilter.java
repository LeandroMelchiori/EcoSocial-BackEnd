package com.alura.foro.hub.api.security.filter;

import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.jwt.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");

        // 1️⃣ No hay header o no es Bearer → request público
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ Extraer token y limpiar
        var token = authHeader.substring(7).trim();

        // 3️⃣ Token vacío → ignorar (NO es un error)
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var subject = tokenService.getSubject(token);

            var user = usuarioRepository.findByUsername(subject)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Token inválido o expirado", ex);
        }
    }

}
