package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.modules.foro.controller.RespuestaHijaController;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosActualizarRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.service.RespuestaHijaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RespuestaHijaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RespuestaHijaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RespuestaHijaService respuestaHijaService;

    // ========= helpers =========

    private Authentication authConUsuario(Long id, String nombre) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(u);
        return auth;
    }

    // ========= tests =========

    @Test
    void listarHijas_devuelve200_yLista() throws Exception {
        Long respuestaId = 5L;

        var salida = List.of(
                new DatosListadoRespuestaHija(
                        10L,
                        "Estoy de acuerdo",
                        "Sacha",
                        LocalDateTime.parse("2025-12-18T19:10:00"),
                        false
                )
        );

        Mockito.when(respuestaHijaService.listarPorRespuesta(respuestaId)).thenReturn(salida);

        mvc.perform(get("/respuestas/{respuestaId}/hijas", respuestaId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].mensaje").value("Estoy de acuerdo"))
                .andExpect(jsonPath("$[0].autorNombre").value("Sacha"))
                .andExpect(jsonPath("$[0].editado").value(false));
    }

    @Test
    void crearHija_devuelve201() throws Exception {
        Long respuestaId = 5L;
        Long userId = 1L;

        var auth = authConUsuario(userId, "Sacha");

        var retorno = new DatosListadoRespuestaHija(
                10L,
                "Estoy de acuerdo",
                "Sacha",
                LocalDateTime.parse("2025-12-18T19:10:00"),
                false
        );

        Mockito.when(respuestaHijaService.crear(
                        eq(respuestaId),
                        any(DatosCrearRespuestaHija.class),
                        eq(userId)
                ))
                .thenReturn(retorno);

        var body = new DatosCrearRespuestaHija("Estoy de acuerdo");

        mvc.perform(post("/respuestas/{respuestaId}/hijas", respuestaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.mensaje").value("Estoy de acuerdo"))
                .andExpect(jsonPath("$.autorNombre").value("Sacha"))
                .andExpect(jsonPath("$.editado").value(false));
    }

    @Test
    void actualizarHija_devuelve200() throws Exception {
        Long hijaId = 10L;
        Long userId = 1L;

        var auth = authConUsuario(userId, "Sacha");

        var retorno = new DatosListadoRespuestaHija(
                hijaId,
                "Actualizado",
                "Sacha",
                LocalDateTime.parse("2025-12-18T19:10:00"),
                true
        );

        Mockito.when(respuestaHijaService.actualizar(
                        eq(hijaId),
                        any(DatosActualizarRespuestaHija.class),
                        eq(userId)
                ))
                .thenReturn(retorno);

        var body = new DatosActualizarRespuestaHija("Actualizado");

        mvc.perform(put("/respuestas/hijas/{id}", hijaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.mensaje").value("Actualizado"))
                .andExpect(jsonPath("$.editado").value(true));
    }

    @Test
    void eliminarHija_devuelve204() throws Exception {
        Long hijaId = 10L;
        Long userId = 1L;

        var auth = authConUsuario(userId, "Sacha");

        Mockito.doNothing().when(respuestaHijaService).eliminar(hijaId, userId);

        mvc.perform(delete("/respuestas/hijas/{id}", hijaId)
                        .principal(auth))
                .andExpect(status().isNoContent());
    }
}
