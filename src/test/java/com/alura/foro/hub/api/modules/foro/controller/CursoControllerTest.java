package com.alura.foro.hub.api.modules.foro.controller;

import com.alura.foro.hub.api.modules.foro.controller.CursoController;
import com.alura.foro.hub.api.modules.foro.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.modules.foro.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.modules.foro.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.modules.foro.service.CursoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CursoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CursoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CursoService cursoService;

    @Test
    @DisplayName("GET /cursos -> 200 y lista de cursos (sin filtro)")
    void listar_sinFiltro() throws Exception {
        var lista = List.of(
                new DatosListadoCurso(1L, "AWS", 2L, "Cloud"),
                new DatosListadoCurso(2L, "Spring Boot", 1L, "Backend")
        );

        when(cursoService.listar(null)).thenReturn(lista);

        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("AWS"))
                .andExpect(jsonPath("$[0].categoriaId").value(2))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Cloud"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(cursoService).listar(null);
        verifyNoMoreInteractions(cursoService);
    }

    @Test
    @DisplayName("GET /cursos?categoriaId=2 -> 200 y pasa el filtro al service")
    void listar_conFiltro() throws Exception {
        var lista = List.of(new DatosListadoCurso(1L, "AWS", 2L, "Cloud"));
        when(cursoService.listar(2L)).thenReturn(lista);

        mvc.perform(get("/cursos").param("categoriaId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaId").value(2));

        verify(cursoService).listar(2L);
        verifyNoMoreInteractions(cursoService);
    }

    @Test
    @DisplayName("GET /cursos/{id} -> 200 y detalle")
    void detallar() throws Exception {
        var dto = new DatosListadoCurso(10L, "Docker", 2L, "Cloud");
        when(cursoService.detallar(10L)).thenReturn(dto);

        mvc.perform(get("/cursos/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Docker"));

        verify(cursoService).detallar(10L);
        verifyNoMoreInteractions(cursoService);
    }

    @Test
    @DisplayName("POST /cursos -> 201, body y Location /cursos/{id}")
    void crear() throws Exception {
        var req = new DatosCrearCurso("Docker", 2L);
        var resp = new DatosListadoCurso(3L, "Docker", 2L, "Cloud");

        when(cursoService.crear(any(DatosCrearCurso.class))).thenReturn(resp);

        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cursos/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Docker"));

        var captor = ArgumentCaptor.forClass(DatosCrearCurso.class);
        verify(cursoService).crear(captor.capture());
        assertThat(captor.getValue().nombre()).isEqualTo("Docker");
        assertThat(captor.getValue().categoriaId()).isEqualTo(2L);

        verifyNoMoreInteractions(cursoService);
    }

    @Test
    @DisplayName("POST /cursos -> 400 si nombre blank")
    void crear_badRequest_nombreBlank() throws Exception {
        var req = new DatosCrearCurso("   ", 2L);

        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cursoService);
    }

    @Test
    @DisplayName("POST /cursos -> 400 si categoriaId null")
    void crear_badRequest_categoriaNull() throws Exception {
        // armamos JSON a mano porque record no permite null fácil en ctor
        var json = """
                { "nombre": "Docker", "categoriaId": null }
                """;

        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cursoService);
    }

    @Test
    @DisplayName("PUT /cursos/{id} -> 200 y devuelve actualizado")
    void actualizar() throws Exception {
        var req = new DatosActualizarCurso("AWS Avanzado", 2L);
        var resp = new DatosListadoCurso(1L, "AWS Avanzado", 2L, "Cloud");

        when(cursoService.actualizar(eq(1L), any(DatosActualizarCurso.class))).thenReturn(resp);

        mvc.perform(put("/cursos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("AWS Avanzado"));

        verify(cursoService).actualizar(eq(1L), any(DatosActualizarCurso.class));
        verifyNoMoreInteractions(cursoService);
    }

    @Test
    @DisplayName("PUT /cursos/{id} -> 400 si nombre blank")
    void actualizar_badRequest_nombreBlank() throws Exception {
        var req = new DatosActualizarCurso("   ", 2L);

        mvc.perform(put("/cursos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cursoService);
    }

    @Test
    @DisplayName("DELETE /cursos/{id} -> 204 y llama service")
    void eliminar() throws Exception {
        doNothing().when(cursoService).eliminar(5L);

        mvc.perform(delete("/cursos/5"))
                .andExpect(status().isNoContent());

        verify(cursoService).eliminar(5L);
        verifyNoMoreInteractions(cursoService);
    }
}
