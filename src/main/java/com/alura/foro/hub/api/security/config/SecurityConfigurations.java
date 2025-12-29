package com.alura.foro.hub.api.security.config;

import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.ApiError;
import com.alura.foro.hub.api.security.filter.RateLimitFilter;
import com.alura.foro.hub.api.security.filter.RateLimitProperties;
import com.alura.foro.hub.api.security.filter.SecurityFilter;
import com.alura.foro.hub.api.security.jwt.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties props) {
        return new RateLimitFilter(props);
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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
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
                                // Accesos publicos
                                .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                                .requestMatchers(HttpMethod.POST,"/usuarios").permitAll()
                                .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/cursos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/topicos/**" ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/respuestas/topico/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/info/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/info/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/respuestas/*/hijas").permitAll()
                                .requestMatchers(HttpMethod.GET, "/respuestas/**").permitAll()

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

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            var body = ApiError.of(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    "Token inválido o ausente.",
                    request.getRequestURI()
            );
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, body);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            var body = ApiError.of(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden",
                    "No tenés permisos para realizar esta acción.",
                    request.getRequestURI()
            );
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, body);
        };
    }
    private final ObjectMapper objectMapper;

    public SecurityConfigurations(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }


}

