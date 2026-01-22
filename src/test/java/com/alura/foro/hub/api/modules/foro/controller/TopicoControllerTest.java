package com.alura.foro.hub.api.modules.foro.controller;

import com.alura.foro.hub.api.modules.foro.controller.TopicoController;
import com.alura.foro.hub.api.modules.foro.dto.topico.*;
import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.service.TopicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TopicoController.class)
class TopicoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TopicoService topicoService;

    private Authentication authConUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("user" + id);
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    // =========================
    // GET /topicos (listar)
    // =========================
    @Test
    void listar_ok_200_y_devuelvePagina() throws Exception {
        var pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());

        var dto = new DatosListadoTopico(
                10L,
                "Consulta sobre monotributo",
                LocalDateTime.of(2025, 12, 18, 18, 30, 0),
                "Leandro",
                1L,
                "Emprendimientos digitales",
                2L,
                "Economía social",
                StatusTopico.ACTIVO,
                1L,
                LocalDateTime.of(2025, 12, 18, 19, 10, 0),
                null
        );

        when(topicoService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        mvc.perform(get("/topicos")
                        .with(authentication(authConUsuario(10L)))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "fechaCreacion,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].titulo").value("Consulta sobre monotributo"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVO"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(topicoService).listar(any(Pageable.class));
    }

    // =========================
    // GET /topicos/{id} (detallar)
    // =========================
    @Test
    void detallar_ok_200() throws Exception {
        var dto = new DatosDetalleTopico(
                10L,
                "Consulta sobre monotributo",
                "¿Cómo facturo si vendo por Instagram?",
                LocalDateTime.of(2025, 12, 18, 18, 30, 0),
                "Leandro",
                1L,
                "Emprendimientos digitales",
                2L,
                "Economía social",
                StatusTopico.ACTIVO,
                null,
                List.of()
        );

        when(topicoService.detallarTopico(10L)).thenReturn(dto);

        mvc.perform(get("/topicos/10")
                        .with(authentication(authConUsuario(10L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Consulta sobre monotributo"))
                .andExpect(jsonPath("$.status").value("ACTIVO"));

        verify(topicoService).detallarTopico(10L);
    }

    // =========================
    // GET /topicos/buscar (buscar)
    // =========================
    @Test
    void buscar_ok_200_y_pasaFiltro() throws Exception {
        var pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());

        var dto = new DatosListadoTopico(
                10L,
                "JWT en Spring",
                LocalDateTime.of(2025, 12, 18, 18, 30, 0),
                "Leandro",
                1L,
                "Spring",
                2L,
                "Backend",
                StatusTopico.ACTIVO,
                0L,
                null,
                null
        );

        when(topicoService.buscar(any(TopicoFiltro.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        mvc.perform(get("/topicos/buscar")
                        .with(authentication(authConUsuario(10L)))
                        .param("q", "jwt")
                        .param("cursoId", "1")
                        .param("autorId", "10")
                        .param("status", "ACTIVO")
                        .param("nombreCurso", "spring")
                        .param("nombreCategoria", "backend")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "fechaCreacion,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].titulo").value("JWT en Spring"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // opcional pero útil: verificar que armó bien el filtro
        ArgumentCaptor<TopicoFiltro> captor = ArgumentCaptor.forClass(TopicoFiltro.class);
        verify(topicoService).buscar(captor.capture(), any(Pageable.class));

        TopicoFiltro f = captor.getValue();
        // si esto falla, el problema está en parámetros / controller, no en service
        org.junit.jupiter.api.Assertions.assertEquals("jwt", f.q());
        org.junit.jupiter.api.Assertions.assertEquals(1L, f.cursoId());
        org.junit.jupiter.api.Assertions.assertEquals(10L, f.autorId());
        org.junit.jupiter.api.Assertions.assertEquals(StatusTopico.ACTIVO, f.status());
        org.junit.jupiter.api.Assertions.assertEquals("spring", f.nombreCurso());
        org.junit.jupiter.api.Assertions.assertEquals("backend", f.nombreCategoria());
    }



    // =========================
    // POST /topicos (crear)
    // =========================
    @Test
    void crear_ok_201_y_llamaService() throws Exception {
        var body = new DatosRegistroTopico(
                "Consulta sobre monotributo",
                "¿Cómo facturo si vendo por Instagram?",
                1L
        );

        var dto = new DatosDetalleTopico(
                10L,
                "Consulta sobre monotributo",
                "¿Cómo facturo si vendo por Instagram?",
                LocalDateTime.of(2025, 12, 18, 18, 30, 0),
                "Leandro",
                1L,
                "Emprendimientos digitales",
                2L,
                "Economía social",
                StatusTopico.ACTIVO,
                null,
                List.of()
        );

        when(topicoService.crearTopico(any(DatosRegistroTopico.class), eq(10L)))
                .thenReturn(dto);

        mvc.perform(post("/topicos")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Consulta sobre monotributo"))
                .andExpect(jsonPath("$.status").value("ACTIVO"));

        verify(topicoService).crearTopico(any(DatosRegistroTopico.class), eq(10L));
    }

    @Test
    void crear_badRequest_siBodyInvalido() throws Exception {
        var json = """
                { "titulo": "", "mensaje": "", "cursoId": null }
                """;

        mvc.perform(post("/topicos")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(topicoService, never()).crearTopico(any(), anyLong());
    }

    // =========================
    // PUT /topicos/{id} (actualizar)
    // =========================
    @Test
    void actualizar_ok_200() throws Exception {
        var body = new DatosActualizarTopico(
                "Consulta sobre monotributo (actualizada)",
                "Ya me inscribí. ¿Cómo emito la primera factura?",
                1L,
                StatusTopico.ACTIVO
        );

        var dto = new DatosDetalleTopico(
                10L,
                "Consulta sobre monotributo (actualizada)",
                "Ya me inscribí. ¿Cómo emito la primera factura?",
                LocalDateTime.of(2025, 12, 18, 18, 30, 0),
                "Leandro",
                1L,
                "Emprendimientos digitales",
                2L,
                "Economía social",
                StatusTopico.ACTIVO,
                null,
                List.of()
        );

        when(topicoService.actualizarTopico(eq(10L), any(DatosActualizarTopico.class), eq(10L)))
                .thenReturn(dto);

        mvc.perform(put("/topicos/10")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Consulta sobre monotributo (actualizada)"));

        verify(topicoService).actualizarTopico(eq(10L), any(DatosActualizarTopico.class), eq(10L));
    }

    @Test
    void actualizar_403_si_service_lanza_forbidden() throws Exception {
        var body = new DatosActualizarTopico(
                "titulo",
                "mensaje",
                1L,
                StatusTopico.ACTIVO
        );

        when(topicoService.actualizarTopico(eq(10L), any(DatosActualizarTopico.class), eq(99L)))
                .thenThrow(new com.alura.foro.hub.api.security.exception.ForbiddenException("Solo el autor puede modificar el tópico"));

        mvc.perform(put("/topicos/10")
                        .with(authentication(authConUsuario(99L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        verify(topicoService).actualizarTopico(eq(10L), any(DatosActualizarTopico.class), eq(99L));
    }

    @Test
    void actualizar_badRequest_siBodyInvalido() throws Exception {
        // Ojo: tu record NO tiene @Validaciones, así que esto puede NO dar 400.
        // Te lo dejo igual por si luego agregás @NotBlank, etc.
        var json = """
                { "titulo": "", "mensaje": "" }
                """;

        mvc.perform(put("/topicos/10")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(topicoService, never()).actualizarTopico(anyLong(), any(), anyLong());
    }

    // =========================
    // DELETE /topicos/{id} (eliminar)
    // =========================
    @Test
    void eliminar_ok_204() throws Exception {
        doNothing().when(topicoService).eliminarTopico(10L, 10L);

        mvc.perform(delete("/topicos/10")
                        .with(authentication(authConUsuario(10L)))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(topicoService).eliminarTopico(10L, 10L);
    }


    @Test
    void eliminar_403_si_service_lanza_forbidden() throws Exception {
        doThrow(new com.alura.foro.hub.api.security.exception.ForbiddenException("Solo el autor del tópico puede eliminarlo"))
                .when(topicoService).eliminarTopico(10L, 99L);

        mvc.perform(delete("/topicos/10")
                        .with(authentication(authConUsuario(99L)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(topicoService).eliminarTopico(10L, 99L);
    }
}
