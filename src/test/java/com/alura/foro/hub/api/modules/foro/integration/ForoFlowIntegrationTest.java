package com.alura.foro.hub.api.modules.foro.integration;

import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.modules.foro.fixtures.ForoHubFixtures;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ForoFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

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

        // 1) Usuario A (autor del tópico) + login
        fx.usuarioConPassword("autor", "123456", passwordEncoder);

        // ⚠️ el fixture genera autor@test.com, no "autor"
        String tokenAutor = loginAndGetToken("autor@test.com", "123456");

        // 2) Crear tópico
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
                .andReturn();

        Long topicoId = readLong(topicoResult.getResponse().getContentAsString(), "id");

        // 3) Usuario B + login
        fx.usuarioConPassword("respondedor", "123456", passwordEncoder);
        String tokenRespondedor = loginAndGetToken("respondedor@test.com", "123456");

        // 4) Crear respuesta
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
                .andReturn();

        Long respuestaId = readLong(respuestaResult.getResponse().getContentAsString(), "id");

        // 5) Marcar solución (autor del tópico)
        mockMvc.perform(
                        patch("/respuestas/{id}/solucion", respuestaId)
                                .header("Authorization", "Bearer " + tokenAutor)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(respuestaId))
                .andExpect(jsonPath("$.solucion").value(true));

        // 6) Verificar
        mockMvc.perform(get("/respuestas/{id}", respuestaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(respuestaId))
                .andExpect(jsonPath("$.solucion").value(true));
    }

    private String loginAndGetToken(String identificador, String password) throws Exception {
        var loginBody = """
        { "identificador": "%s", "password": "%s" }
        """.formatted(identificador, password);

        var loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private Long readLong(String json, String field) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get(field).asLong();
    }
}
