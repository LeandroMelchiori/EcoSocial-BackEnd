package com.alura.foro.hub.api.modules.foro.security;

import com.alura.foro.hub.api.modules.foro.controller.RespuestaHijaController;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.modules.foro.service.RespuestaHijaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RespuestaHijaController.class)
@ActiveProfiles("test")
class RespuestaHijaControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RespuestaHijaService respuestaHijaService;

    @Test
    void crearRespuestaHija_sinToken_debeDar401() throws Exception {
        var body = """
            { "mensaje": "hola" }
        """;

        mvc.perform(post("/respuestas/{respuestaId}/hijas", 1L)
                        .with(csrf()) // <- clave
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        Mockito.verify(respuestaHijaService, Mockito.never())
                .crear(anyLong(), any(), anyLong());
    }

    @WithMockUser(roles = "USER")
    @Test
    void editarRespuestaHija_sinPermisos_debeDar403() throws Exception {
        // OJO: este test solo sirve si tu controller NO castea principal a Usuario.
        // Si casteás, con WithMockUser te va a tirar ClassCastException.
        Mockito.when(respuestaHijaService.actualizar(eq(10L), any(), anyLong()))
                .thenThrow(new ForbiddenException("Solo el autor puede editar esta respuesta hija"));

        var body = """
            { "mensaje": "intento editar" }
        """;

        mvc.perform(put("/respuestas/hijas/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "USER")
    @Test
    void eliminarRespuestaHija_sinPermisos_debeDar403() throws Exception {
        Mockito.doThrow(new ForbiddenException("No tenés permisos para eliminar esta respuesta hija"))
                .when(respuestaHijaService).eliminar(eq(10L), anyLong());

        mvc.perform(delete("/respuestas/hijas/{id}", 10L))
                .andExpect(status().isForbidden());
    }
}

