package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    @DisplayName("GET /categorias -> 200 y lista")
    void listar() throws Exception {
        var lista = List.of(
                new DatosListadoCategoria(1L, "Backend"),
                new DatosListadoCategoria(2L, "Cloud")
        );

        when(categoriaService.listar()).thenReturn(lista);

        mvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Backend"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(categoriaService).listar();
        verifyNoMoreInteractions(categoriaService);
    }

    @Test
    @DisplayName("GET /categorias/{id}/cursos -> 200 y lista cursos por categoria")
    void listarCursosDeCategoria() throws Exception {
        var lista = List.of(
                new DatosListadoCurso(1L, "AWS", 2L, "Cloud")
        );

        when(categoriaService.listarCursosDeCategoria(2L)).thenReturn(lista);

        mvc.perform(get("/categorias/2/cursos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoriaId").value(2))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Cloud"));

        verify(categoriaService).listarCursosDeCategoria(2L);
        verifyNoMoreInteractions(categoriaService);
    }

    @Test
    @DisplayName("POST /categorias -> 201, body y Location /categorias/{id}")
    void crear() throws Exception {
        var req = new DatosCrearCategoria("DevOps");
        var resp = new DatosListadoCategoria(3L, "DevOps");

        when(categoriaService.crear(any(DatosCrearCategoria.class))).thenReturn(resp);

        mvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categorias/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("DevOps"));

        var captor = ArgumentCaptor.forClass(DatosCrearCategoria.class);
        verify(categoriaService).crear(captor.capture());
        assertThat(captor.getValue().nombre()).isEqualTo("DevOps");

        verifyNoMoreInteractions(categoriaService);
    }

    @Test
    @DisplayName("POST /categorias -> 400 si nombre blank")
    void crear_badRequest_nombreBlank() throws Exception {
        var req = new DatosCrearCategoria("   ");

        mvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @DisplayName("PUT /categorias/{id} -> 200 y devuelve actualizado")
    void actualizar() throws Exception {
        var req = new DatosActualizarCategoria("Cloud");
        var resp = new DatosListadoCategoria(2L, "Cloud");

        when(categoriaService.actualizar(eq(2L), any(DatosActualizarCategoria.class))).thenReturn(resp);

        mvc.perform(put("/categorias/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Cloud"));

        verify(categoriaService).actualizar(eq(2L), any(DatosActualizarCategoria.class));
        verifyNoMoreInteractions(categoriaService);
    }

    @Test
    @DisplayName("PUT /categorias/{id} -> 400 si nombre blank")
    void actualizar_badRequest_nombreBlank() throws Exception {
        var req = new DatosActualizarCategoria("   ");

        mvc.perform(put("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @DisplayName("DELETE /categorias/{id} -> 204 y llama service")
    void eliminar() throws Exception {
        doNothing().when(categoriaService).eliminar(9L);

        mvc.perform(delete("/categorias/9"))
                .andExpect(status().isNoContent());

        verify(categoriaService).eliminar(9L);
        verifyNoMoreInteractions(categoriaService);
    }
}
