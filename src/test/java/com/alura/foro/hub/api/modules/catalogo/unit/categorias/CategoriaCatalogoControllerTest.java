package com.alura.foro.hub.api.modules.catalogo.unit.categorias;

import com.alura.foro.hub.api.modules.catalogo.controller.CategoriaCatalogoController;
import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosDetalleCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosDetalleSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.service.CategoriaCatalogoService;
import com.alura.foro.hub.api.security.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoriaCatalogoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CategoriaCatalogoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    CategoriaCatalogoService service;

    private static DatosDetalleCategoriaProducto cat(Long id, String nombre, boolean activo) {
        return new DatosDetalleCategoriaProducto(id, nombre, activo);
    }

    private static DatosDetalleSubcategoriaProducto sub(Long id, Long categoriaId, String nombre, boolean activo) {
        return new DatosDetalleSubcategoriaProducto(id, categoriaId, nombre, activo);
    }

    @Test
    void categorias_ok_200() throws Exception {
        when(service.listarCategoriasActivas())
                .thenReturn(List.of(
                        cat(1L, "Hogar", true),
                        cat(2L, "Servicios", true)
                ));

        mockMvc.perform(get("/catalogo/categorias")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Hogar"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Servicios"))
                .andExpect(jsonPath("$[1].activo").value(true));

        verify(service).listarCategoriasActivas();
        verifyNoMoreInteractions(service);
    }

    @Test
    void subcategorias_ok_200() throws Exception {
        when(service.listarSubcategoriasActivas(10L))
                .thenReturn(List.of(
                        sub(100L, 10L, "Sillas", true),
                        sub(101L, 10L, "Mesas", true)
                ));

        mockMvc.perform(get("/catalogo/categorias/{categoriaId}/subcategorias", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].categoriaId").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Sillas"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].id").value(101))
                .andExpect(jsonPath("$[1].categoriaId").value(10))
                .andExpect(jsonPath("$[1].nombre").value("Mesas"))
                .andExpect(jsonPath("$[1].activo").value(true));

        verify(service).listarSubcategoriasActivas(10L);
        verifyNoMoreInteractions(service);
    }
}
