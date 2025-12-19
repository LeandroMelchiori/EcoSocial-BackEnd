package com.alura.foro.hub.api.security.config;

import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.filter.RateLimitFilter;
import com.alura.foro.hub.api.security.filter.SecurityFilter;
import com.alura.foro.hub.api.security.jwt.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    @Bean
    public SecurityFilter securityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        return new SecurityFilter(tokenService, usuarioRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RateLimitFilter rateLimitFilter,
            SecurityFilter securityFilter) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .cacheControl(withDefaults())
                        .contentTypeOptions(withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // Política de referer (reduce fuga de info)
                        .referrerPolicy(ref -> ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        // CSP
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src * 'unsafe-inline' 'unsafe-eval' data: blob:"
                                )
                        )
                        // Permissions-Policy
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy",
                                "geolocation=(), microphone=(), camera=(), payment=(), usb=()"
                        ))
                )
                // Authorizations EndPoints
                .authorizeHttpRequests((authorize) -> authorize
                                // Login - Crear usuario - Listados foro (Acceso general)
                                .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                                .requestMatchers(HttpMethod.POST,"/usuarios").permitAll()
                                .requestMatchers(HttpMethod.GET, "/categorias/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/cursos/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/topicos" ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/respuestas").permitAll()

                                // 🔒 C.U.D CATEGORIAS → SOLO ADMIN
                                .requestMatchers(HttpMethod.POST, "/categorias/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/categorias/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")

                                // 🔒 C.U.D CURSOS → SOLO ADMIN
                                .requestMatchers(HttpMethod.POST, "/cursos/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/cursos/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/cursos/**").hasRole("ADMIN")

                                // Swagger UI y Docs
                                .requestMatchers(
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/swagger-ui/oauth2-redirect.html",
                                        "/webjars/**"
                                ).permitAll()
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

