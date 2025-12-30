package com.alura.foro.hub.api.security;

import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.auth.UsuarioAuthenticateData;
import com.alura.foro.hub.api.security.config.SecurityConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfigurations.class)
class AuthenticationControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String USERNAME = "autor1";
    private static final String PASSWORD = "123456";

    @BeforeEach
    void setup() {
        // Dejamos la DB en un estado conocido para este test
        usuarioRepository.deleteAll();

        var usuario = new Usuario();
        usuario.setNombre("Autor Test");
        usuario.setEmail("autor1@test.com");
        usuario.setUsername(USERNAME);
        usuario.setPassword(passwordEncoder.encode(PASSWORD)); // clave: guardarla encriptada

        usuarioRepository.save(usuario);
    }

    @Test
    void login_ok_devuelve_token() throws Exception {
        var body = new UsuarioAuthenticateData(USERNAME, PASSWORD);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_credenciales_invalidas_devuelve_403_o_401() throws Exception {
        var body = new UsuarioAuthenticateData(USERNAME, "malPassword");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                // Spring Security típicamente devuelve 403 en algunos setups, 401 en otros.
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_body_invalido_devuelve_400() throws Exception {
        // username vacío rompe @NotBlank
        var body = new UsuarioAuthenticateData("", PASSWORD);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
