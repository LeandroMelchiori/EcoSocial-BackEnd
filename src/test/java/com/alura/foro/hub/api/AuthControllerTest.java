package com.alura.foro.hub.api;

import com.alura.foro.hub.api.controller.AuthController;
import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("usuario_test");
        usuario.setEmail("test@example.com");
        usuario.setPassword(new BCryptPasswordEncoder().encode("password"));
    }

    @Test
    void testLoginExitoso() throws Exception {
        when(usuarioRepository.findByUsername("usuario_test")).thenReturn(java.util.Optional.of(usuario));
        when(jwtUtil.generateToken("usuario_test")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .param("username", "usuario_test")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked-jwt-token"));
    }

    @Test
    void testLoginFallido() throws Exception {
        when(usuarioRepository.findByUsername("usuario_test")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .param("username", "usuario_test")
                        .param("password", "password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegistroUsuario() throws Exception {
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "username": "usuario_test",
                        "email": "test@example.com",
                        "password": "password"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usuario_test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
