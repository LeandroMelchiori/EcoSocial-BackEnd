package com.alura.foro.hub.api.modules.foro.controller;

import com.alura.foro.hub.api.modules.foro.controller.RespuestaController;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.service.RespuestaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RespuestaController.class)
class RespuestaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RespuestaService respuestaService;

    private Authentication authConUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("user" + id); // opcional, pero útil si algo lo loggea
        // No hace falta setPassword ni perfiles para este test de controller

        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    // =========================
    // POST /respuestas (crear)
    // =========================
    @Test
    void crear_ok_201_y_llamaService() throws Exception {
        var body = new DatosCrearRespuesta(10L, "Respuesta");

        var dto = new DatosListadoRespuesta(
                45L,
                "Respuesta",
                "Otro Usuario",
                false,
                LocalDateTime.of(2025, 12, 18, 19, 10, 0),
                0L,
                null
        );

        when(respuestaService.crear(any(DatosCrearRespuesta.class), eq(10L)))
                .thenReturn(dto);

        mvc.perform(post("/respuestas")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(45))
                .andExpect(jsonPath("$.mensaje").value("Respuesta"))
                .andExpect(jsonPath("$.solucion").value(false));

        verify(respuestaService).crear(any(DatosCrearRespuesta.class), eq(10L));
    }

    @Test
    void crear_badRequest_siBodyInvalido() throws Exception {
        var json = """
        { "topicoId": null, "mensaje": "" }
        """;

        mvc.perform(post("/respuestas")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(respuestaService, never()).crear(any(), anyLong());
    }

    // =========================
    // GET /respuestas/topico/{topicoId} (listar)
    // =========================
    @Test
    void listar_ok_200_y_devuelvePagina() throws Exception {
        var pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());

        var dto = new DatosListadoRespuesta(
                45L,
                "Mensaje",
                "Otro Usuario",
                false,
                LocalDateTime.now(),
                0L,
                null
        );

        when(respuestaService.listarPorTopico(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        mvc.perform(get("/respuestas/topico/10")
                        .with(authentication(authConUsuario(10L)))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "fechaCreacion,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(45))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(respuestaService).listarPorTopico(eq(10L), any(Pageable.class));
    }

    // =========================
    // PATCH /respuestas/{id}/solucion
    // =========================
    @Test
    void marcarSolucion_ok_200() throws Exception {
        var dto = new DatosListadoRespuesta(
                45L,
                "Mensaje",
                "Otro Usuario",
                true,
                LocalDateTime.now(),
                0L,
                null
        );

        when(respuestaService.marcarSolucion(eq(45L), eq(10L))).thenReturn(dto);

        mvc.perform(patch("/respuestas/45/solucion")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(45))
                .andExpect(jsonPath("$.solucion").value(true));

        verify(respuestaService).marcarSolucion(45L, 10L);
    }

    // =========================
    // PUT /respuestas/{id}
    // =========================
    @Test
    void editar_ok_200() throws Exception {
        var body = new DatosActualizarRespuesta("Actualicé la respuesta");

        var dto = new DatosListadoRespuesta(
                45L,
                "Actualicé la respuesta",
                "Otro Usuario",
                false,
                LocalDateTime.now(),
                0L,
                null
        );

        when(respuestaService.actualizar(eq(45L), any(DatosActualizarRespuesta.class), eq(10L)))
                .thenReturn(dto);

        mvc.perform(put("/respuestas/45")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(45))
                .andExpect(jsonPath("$.mensaje").value("Actualicé la respuesta"));

        verify(respuestaService).actualizar(eq(45L), any(DatosActualizarRespuesta.class), eq(10L));
    }

    @Test
    void editar_badRequest_siMensajeVacio() throws Exception {
        var json = """
        { "mensaje": "" }
        """;

        mvc.perform(put("/respuestas/45")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(respuestaService, never()).actualizar(anyLong(), any(), anyLong());
    }

    // =========================
    // DELETE /respuestas/{id}
    // =========================
    @Test
    void eliminar_ok_204() throws Exception {
        doNothing().when(respuestaService).eliminar(45L, 10L);

        mvc.perform(delete("/respuestas/45")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(respuestaService).eliminar(45L, 10L);
    }
}

