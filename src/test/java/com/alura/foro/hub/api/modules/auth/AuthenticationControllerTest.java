package com.alura.foro.hub.api.modules.auth;

import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.auth.UsuarioAuthenticateData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    ProductoRepository productoRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PerfilEmprendimientoRepository perfilEmprendimientoRepository;

    private static final String EMAIL = "autor1@test.com";
    private static final String DNI = "12345678";
    private static final String PASSWORD = "123456";

    @BeforeEach
    void setup() {
        productoRepository.deleteAll();
        perfilEmprendimientoRepository.deleteAll();
        usuarioRepository.deleteAll();

        var usuario = new Usuario();
        usuario.setNombre("Autor");
        usuario.setApellido("Test");
        usuario.setDni(DNI);
        usuario.setEmail(EMAIL);
        usuario.setPassword(passwordEncoder.encode(PASSWORD));

        usuarioRepository.save(usuario);
    }

    @Test
    void login_ok_por_email_devuelve_token() throws Exception {
        var body = new UsuarioAuthenticateData(EMAIL, PASSWORD);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_ok_por_dni_devuelve_token() throws Exception {
        var body = new UsuarioAuthenticateData(DNI, PASSWORD);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_credenciales_invalidas_devuelve_403_o_401() throws Exception {
        var body = new UsuarioAuthenticateData(EMAIL, "malPassword");

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
