package com.alura.foro.hub.api.security.config;

import com.alura.foro.hub.api.user.domain.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
                return Optional.empty();
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof Usuario usuario) {
                return Optional.of(usuario.getId());
            }

            return Optional.empty();
        };
    }
}
