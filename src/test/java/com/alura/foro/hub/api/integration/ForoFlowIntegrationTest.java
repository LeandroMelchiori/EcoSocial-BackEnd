package com.alura.foro.hub.api.integration;

import com.alura.foro.hub.api.entity.model.Categoria;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.fixtures.ForoHubFixtures;
import com.alura.foro.hub.api.repository.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ForoFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TopicoRepository topicoRepository;
    @Autowired RespuestaRepository respuestaRepository;
    @Autowired RespuestaHijaRepository respuestaHijaRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired CursoRepository cursoRepository;

    @Autowired PasswordEncoder passwordEncoder;

    private ForoHubFixtures fx;
    private Long cursoId;

    @BeforeEach
    void setup() {
        fx = new ForoHubFixtures(usuarioRepository, topicoRepository, respuestaRepository, respuestaHijaRepository);

        // ✅ Creamos categoría + curso para que /topicos no falle con "Curso no encontrado"
        var categoria = new Categoria();
        categoria.setNombre("Categoria test");
        categoriaRepository.saveAndFlush(categoria);

        var curso = new Curso();
        curso.setNombre("Curso test");
        curso.setCategoria(categoria);
        cursoId = cursoRepository.saveAndFlush(curso).getId();
    }

    @Test
    void flujo_principal_login_crearTopico_responder_marcarSolucion_y_verificar() throws Exception {

        // =========================
        // 1) Usuario A (autor del tópico) + login
        // =========================
        fx.usuarioConPassword("autor", "123456", passwordEncoder);

        String tokenAutor = loginAndGetToken("autor", "123456");

        // =========================
        // 2) Crear tópico con tokenAutor
        // =========================
        var crearTopicoBody = """
            {
              "titulo": "Topico de flujo",
              "mensaje": "Mensaje del topico (flow test)",
              "cursoId": %d
            }
            """.formatted(cursoId);

        var topicoResult = mockMvc.perform(
                        post("/topicos")
                                .header("Authorization", "Bearer " + tokenAutor)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(crearTopicoBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Topico de flujo"))
                .andReturn();

        Long topicoId = readLong(topicoResult.getResponse().getContentAsString(), "id");

        // =========================
        // 3) Usuario B (responde) + login
        // =========================
        fx.usuarioConPassword("respondedor", "123456", passwordEncoder);
        String tokenRespondedor = loginAndGetToken("respondedor", "123456");

        // =========================
        // 4) Crear respuesta al tópico con tokenRespondedor
        // =========================
        var crearRespuestaBody = """
            {
              "topicoId": %d,
              "mensaje": "Respuesta del usuario B"
            }
            """.formatted(topicoId);

        var respuestaResult = mockMvc.perform(
                        post("/respuestas")
                                .header("Authorization", "Bearer " + tokenRespondedor)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(crearRespuestaBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.mensaje").value("Respuesta del usuario B"))
                .andExpect(jsonPath("$.solucion").value(false))
                .andReturn();

        Long respuestaId = readLong(respuestaResult.getResponse().getContentAsString(), "id");

        // =========================
        // 5) Marcar respuesta como solución con tokenAutor (solo autor del tópico)
        // =========================
        mockMvc.perform(
                        patch("/respuestas/{id}/solucion", respuestaId)
                                .header("Authorization", "Bearer " + tokenAutor)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(respuestaId))
                .andExpect(jsonPath("$.solucion").value(true));

        // =========================
        // 6) Verificar detalle de respuesta (solucion=true)
        // =========================
        mockMvc.perform(get("/respuestas/{id}", respuestaId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(respuestaId))
                .andExpect(jsonPath("$.solucion").value(true));

        // (Opcional) Podés también verificar que el tópico muestra la respuesta, si tu DTO de detalle la incluye.
        mockMvc.perform(get("/topicos/{id}", topicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicoId))
                .andExpect(jsonPath("$.titulo").value("Topico de flujo"));
    }

    // =========================
    // Helpers
    // =========================
    private String loginAndGetToken(String username, String password) throws Exception {
        var loginBody = """
            { "username": "%s", "password": "%s" }
            """.formatted(username, password);

        var loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private Long readLong(String json, String field) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get(field).asLong();
    }
}
