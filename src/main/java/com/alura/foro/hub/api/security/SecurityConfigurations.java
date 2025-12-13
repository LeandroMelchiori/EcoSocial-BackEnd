package com.alura.foro.hub.api.security;

import com.alura.foro.hub.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // le indicamos a Spring el tipo de sesion
                .authorizeHttpRequests((authorize) -> authorize

                                // Login - Crear usuario (Acceso general)
                                .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                                .requestMatchers(HttpMethod.POST,"/usuarios").permitAll()

                                // 📚 LISTADOS (USER o ADMIN)
                                .requestMatchers(HttpMethod.GET, "/categorias/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/cursos/**").authenticated()

                                // 🔒 CRUD CATEGORIAS → SOLO ADMIN
                                .requestMatchers(HttpMethod.POST, "/categorias/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/categorias/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")

                                // 🔒 CRUD CURSOS → SOLO ADMIN
                                .requestMatchers(HttpMethod.POST, "/cursos/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/cursos/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/cursos/**").hasRole("ADMIN")

                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**","/swagger-ui.html").permitAll()
                                .anyRequest()
                                .authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) // llamada a nuestro filtro antes que el de spring
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

