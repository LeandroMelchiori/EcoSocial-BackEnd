package com.alura.foro.hub.api;

import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.fixtures.ForoHubFixtures;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaRepository;
import com.alura.foro.hub.api.modules.foro.repository.TopicoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RespuestaHijaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TopicoRepository topicoRepository;
    @Autowired RespuestaRepository respuestaRepository;
    @Autowired
    RespuestaHijaRepository respuestaHijaRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private ForoHubFixtures fx;
    private Long respuestaId;
    private Long topicoId;
    private Long respuestaHijaId;

    private Usuario tester;

    @BeforeEach
    void setup() {
        fx = new ForoHubFixtures(usuarioRepository, topicoRepository, respuestaRepository, respuestaHijaRepository);

        tester = fx.usuario("tester");
        Usuario intruso = fx.usuario("intruso");

        var topico = fx.topico(tester);
        topicoId = topico.getId();

        var respuesta = fx.respuesta(topico, tester);
        respuestaId = respuesta.getId();

        var hija = fx.respuestaHija(respuesta, tester);
        respuestaHijaId = hija.getId();
    }

    @WithUserDetails(
            value = "tester",
            userDetailsServiceBeanName = "authenticationService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    @Test
    void crearRespuestaHija_ok_integracion() throws Exception {

        var body = """
        { "mensaje": "Respuesta hija desde integration test" }
        """;

        mockMvc.perform(
                        post("/respuestas/{id}/hijas", respuestaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                // asserts típicos (ajustá los paths si tu DTO usa otros nombres)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.mensaje").value("Respuesta hija desde integration test"));
    }

    @WithUserDetails(
            value = "tester",
            userDetailsServiceBeanName = "authenticationService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    @Test
    void crearRespuestaHija_topicoCerrado_debeDar400() throws Exception {
        // cerramos el tópico antes de llamar
        var topico = topicoRepository.findById(topicoId).orElseThrow();
        topico.setStatus(StatusTopico.CERRADO);
        topicoRepository.saveAndFlush(topico);

        var body = """
        { "mensaje": "No debería poder crearse" }
        """;

        mockMvc.perform(
                        post("/respuestas/{id}/hijas", respuestaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearRespuestaHija_sinAuth_debeDar401() throws Exception {
        var body = """
        { "mensaje": "Sin auth" }
        """;

        mockMvc.perform(
                        post("/respuestas/{id}/hijas", respuestaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(
            value = "intruso",
            userDetailsServiceBeanName = "authenticationService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void eliminarRespuestaHija_forbidden_siNoEsAutorNiAutorTopicoNiAdmin() throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/respuestas/hijas/{id}", respuestaHijaId)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    @WithUserDetails(
            value = "tester",
            userDetailsServiceBeanName = "authenticationService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void crearRespuestaHija_notFound_siRespuestaPadreNoExiste() throws Exception {

        var body = """
        { "mensaje": "Intento sobre respuesta inexistente" }
        """;

        Long respuestaInexistenteId = 999999L;

        mockMvc.perform(
                        post("/respuestas/{id}/hijas", respuestaInexistenteId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }


}

