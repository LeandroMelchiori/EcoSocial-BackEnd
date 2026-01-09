package com.alura.foro.hub.api.integration;

import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.fixtures.ForoHubFixtures;
import com.alura.foro.hub.api.modules.foro.repository.*;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
@Transactional
class AuthenticationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    TopicoRepository topicoRepository;
    @Autowired
    RespuestaRepository respuestaRepository;
    @Autowired
    RespuestaHijaRepository respuestaHijaRepository;
    @Autowired
    CategoriaRepository categoriaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired
    CursoRepository cursoRepository;
    @Autowired ObjectMapper objectMapper;

    private ForoHubFixtures fx;
    private Long cursoId;

    @BeforeEach
    void setup() {
        fx = new ForoHubFixtures(usuarioRepository, topicoRepository, respuestaRepository, respuestaHijaRepository);
        var categoria = new Categoria();
        var curso = new Curso();
        categoria.setNombre("Categoria test");
        categoriaRepository.saveAndFlush(categoria);
        curso.setNombre("Curso test");
        curso.setCategoria(categoria);
        cursoId = cursoRepository.saveAndFlush(curso).getId();
    }

    @Test
    void login_ok_devuelveToken() throws Exception {
        fx.usuarioConPassword("tester", "123456", passwordEncoder);

        var body = """
            { "username": "tester", "password": "123456" }
        """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_fail_passwordIncorrecta_da401() throws Exception {
        fx.usuarioConPassword("tester", "123456", passwordEncoder);

        var body = """
            { "username": "tester", "password": "mala" }
        """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_y_con_token_puedo_crear_topico_endpoint_protegido() throws Exception {
        var tester = fx.usuarioConPassword("tester", "123456", passwordEncoder);

        // 1) Login y extraer token
        var loginBody = """
            { "username": "tester", "password": "123456" }
        """;

        var loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        String token = node.get("token").asText();

        // 2) Usar token en endpoint protegido: POST /topicos
        var crearTopicoBody = """
            {
              "titulo": "Topico desde test auth",
              "mensaje": "Probando token real",
              "cursoId": %d
            }
            """.formatted(this.cursoId);


        mockMvc.perform(
                        post("/topicos")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(crearTopicoBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Topico desde test auth"));
    }
}
