package com.alura.foro.hub.api.security;

import com.alura.foro.hub.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Obtener el authHeader del header
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            var token = authHeader.replace("Bearer ", "");
//            System.out.println(tokenService.getSubject(token)); // este usuario tiene sesion?
            var subject = tokenService.getSubject(token); // extract username
            if (subject != null) {
                //Token valido
                var user = usersRepository.findByUsername(subject); // subject = nombre de usuario
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.get().getAuthorities()); // forzamos un inicio de sesion
                SecurityContextHolder.getContext().setAuthentication(authentication); // seteamos la autenticacion
            }
        }
        filterChain.doFilter(request, response);
    }
}