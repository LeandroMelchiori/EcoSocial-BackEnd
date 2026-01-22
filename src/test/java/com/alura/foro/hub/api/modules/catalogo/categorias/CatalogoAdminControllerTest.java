package com.alura.foro.hub.api.modules.catalogo.categorias;

import com.alura.foro.hub.api.modules.catalogo.controller.CatalogoAdminController;
import com.alura.foro.hub.api.TestSecurityConfig;
import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosActualizarCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosCrearCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosDetalleCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosActualizarSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosCrearSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosDetalleSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.service.CatalogoAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CatalogoAdminController.class)
@Import(TestSecurityConfig.class)
class CatalogoAdminControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    CatalogoAdminService service;

    // ============================
    // ========== CATEGORIAS =======
    // ============================

    @Test
    @DisplayName("ADMIN: POST /catalogo/admin/categorias -> 201 + Location + body")
    @WithMockUser(roles = "ADMIN")
    void crearCategoria_admin_ok_201() throws Exception {
        var dto = new DatosCrearCategoriaProducto("Electrónica");
        var creado = new DatosDetalleCategoriaProducto(10L, "Electrónica", true);

        Mockito.when(service.crearCategoria(any(DatosCrearCategoriaProducto.class)))
                .thenReturn(creado);

        mvc.perform(post("/catalogo/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/catalogo/categorias/10"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Electrónica"))
                .andExpect(jsonPath("$.activo").value(true));

        Mockito.verify(service, times(1)).crearCategoria(any(DatosCrearCategoriaProducto.class));
    }

    @Test
    @DisplayName("USER: POST /catalogo/admin/categorias -> 403")
    @WithMockUser(roles = "USER")
    void crearCategoria_user_forbidden_403() throws Exception {
        var dto = new DatosCrearCategoriaProducto("Electrónica");

        mvc.perform(post("/catalogo/admin/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        Mockito.verify(service, never()).crearCategoria(any());
    }

    @Test
    @DisplayName("ADMIN: PUT /catalogo/admin/categorias/{id} -> 200")
    @WithMockUser(roles = "ADMIN")
    void actualizarCategoria_admin_ok_200() throws Exception {
        Long id = 10L;
        var dto = new DatosActualizarCategoriaProducto("Nueva");
        var actualizado = new DatosDetalleCategoriaProducto(10L, "Nueva", true);

        Mockito.when(service.actualizarCategoria(eq(id), any(DatosActualizarCategoriaProducto.class)))
                .thenReturn(actualizado);

        mvc.perform(put("/catalogo/admin/categorias/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Nueva"));

        Mockito.verify(service, times(1)).actualizarCategoria(eq(id), any(DatosActualizarCategoriaProducto.class));
    }

    @Test
    @DisplayName("ADMIN: PATCH /catalogo/admin/categorias/{id}/activar -> 204")
    @WithMockUser(roles = "ADMIN")
    void activarCategoria_admin_ok_204() throws Exception {
        Long id = 10L;

        mvc.perform(patch("/catalogo/admin/categorias/{id}/activar", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).activarCategoria(id);
    }

    @Test
    @DisplayName("ADMIN: PATCH /catalogo/admin/categorias/{id}/desactivar -> 204")
    @WithMockUser(roles = "ADMIN")
    void desactivarCategoria_admin_ok_204() throws Exception {
        Long id = 10L;

        mvc.perform(patch("/catalogo/admin/categorias/{id}/desactivar", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).desactivarCategoria(id);
    }

    @Test
    @DisplayName("ADMIN: DELETE /catalogo/admin/categorias/{id} -> 204")
    @WithMockUser(roles = "ADMIN")
    void eliminarCategoria_admin_ok_204() throws Exception {
        Long id = 10L;

        mvc.perform(delete("/catalogo/admin/categorias/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).eliminarCategoria(id);
    }

    // ============================
    // ======== SUBCATEGORIAS ======
    // ============================

    @Test
    @DisplayName("ADMIN: POST /catalogo/admin/subcategorias -> 201 + Location + body")
    @WithMockUser(roles = "ADMIN")
    void crearSubcategoria_admin_ok_201() throws Exception {
        var dto = new DatosCrearSubcategoriaProducto(10L, "Celulares");
        var creado = new DatosDetalleSubcategoriaProducto(77L, 10L, "Celulares", true);

        Mockito.when(service.crearSubcategoria(any(DatosCrearSubcategoriaProducto.class)))
                .thenReturn(creado);

        mvc.perform(post("/catalogo/admin/subcategorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/catalogo/categorias/10/subcategorias"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.categoriaId").value(10))
                .andExpect(jsonPath("$.nombre").value("Celulares"))
                .andExpect(jsonPath("$.activo").value(true));

        Mockito.verify(service, times(1)).crearSubcategoria(any(DatosCrearSubcategoriaProducto.class));
    }

    @Test
    @DisplayName("ADMIN: PUT /catalogo/admin/subcategorias/{id} -> 200")
    @WithMockUser(roles = "ADMIN")
    void actualizarSubcategoria_admin_ok_200() throws Exception {
        Long id = 77L;

        // tu record: (categoriaId, nombre)
        var dto = new DatosActualizarSubcategoriaProducto(10L, "Smartphones");
        var actualizado = new DatosDetalleSubcategoriaProducto(77L, 10L, "Smartphones", true);

        Mockito.when(service.actualizarSubcategoria(eq(id), any(DatosActualizarSubcategoriaProducto.class)))
                .thenReturn(actualizado);

        mvc.perform(put("/catalogo/admin/subcategorias/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.categoriaId").value(10))
                .andExpect(jsonPath("$.nombre").value("Smartphones"));

        Mockito.verify(service, times(1)).actualizarSubcategoria(eq(id), any(DatosActualizarSubcategoriaProducto.class));
    }

    @Test
    @DisplayName("ADMIN: PATCH /catalogo/admin/subcategorias/{id}/activar -> 204")
    @WithMockUser(roles = "ADMIN")
    void activarSubcategoria_admin_ok_204() throws Exception {
        Long id = 77L;

        mvc.perform(patch("/catalogo/admin/subcategorias/{id}/activar", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).activarSubcategoria(id);
    }

    @Test
    @DisplayName("ADMIN: PATCH /catalogo/admin/subcategorias/{id}/desactivar -> 204")
    @WithMockUser(roles = "ADMIN")
    void desactivarSubcategoria_admin_ok_204() throws Exception {
        Long id = 77L;

        mvc.perform(patch("/catalogo/admin/subcategorias/{id}/desactivar", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).desactivarSubcategoria(id);
    }

    @Test
    @DisplayName("ADMIN: DELETE /catalogo/admin/subcategorias/{id} -> 204")
    @WithMockUser(roles = "ADMIN")
    void eliminarSubcategoria_admin_ok_204() throws Exception {
        Long id = 77L;

        mvc.perform(delete("/catalogo/admin/subcategorias/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(service, times(1)).eliminarSubcategoria(id);
    }

    // ============================
    // ===== LISTADOS / DETALLES ===
    // ============================

    @Test
    @DisplayName("ADMIN: GET /catalogo/admin/categorias -> 200 + lista")
    @WithMockUser(roles = "ADMIN")
    void listarCategoriasAdmin_ok_200() throws Exception {
        var lista = List.of(
                new DatosDetalleCategoriaProducto(1L, "A", true),
                new DatosDetalleCategoriaProducto(2L, "B", false)
        );

        Mockito.when(service.listarCategoriasAdmin()).thenReturn(lista);

        mvc.perform(get("/catalogo/admin/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        Mockito.verify(service, times(1)).listarCategoriasAdmin();
    }

    @Test
    @DisplayName("ADMIN: GET /catalogo/admin/subcategorias?categoriaId=10 -> 200 + lista")
    @WithMockUser(roles = "ADMIN")
    void listarSubcategoriasAdmin_filtrada_ok_200() throws Exception {
        var lista = List.of(
                new DatosDetalleSubcategoriaProducto(77L, 10L, "Celulares", true)
        );

        Mockito.when(service.listarSubcategoriasAdmin(eq(10L))).thenReturn(lista);

        mvc.perform(get("/catalogo/admin/subcategorias")
                        .param("categoriaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoriaId").value(10));

        Mockito.verify(service, times(1)).listarSubcategoriasAdmin(10L);
    }

    @Test
    @DisplayName("ADMIN: GET /catalogo/admin/subcategorias -> 200 + lista (sin filtro)")
    @WithMockUser(roles = "ADMIN")
    void listarSubcategoriasAdmin_sinFiltro_ok_200() throws Exception {
        var lista = List.of(
                new DatosDetalleSubcategoriaProducto(77L, 10L, "Celulares", true),
                new DatosDetalleSubcategoriaProducto(78L, 10L, "Accesorios", true)
        );

        Mockito.when(service.listarSubcategoriasAdmin(isNull())).thenReturn(lista);

        mvc.perform(get("/catalogo/admin/subcategorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        Mockito.verify(service, times(1)).listarSubcategoriasAdmin(isNull());
    }

    @Test
    @DisplayName("ADMIN: GET /catalogo/admin/subcategorias/{id} -> 200 + detalle")
    @WithMockUser(roles = "ADMIN")
    void detalleSubcategoriaAdmin_ok_200() throws Exception {
        Long id = 77L;
        var detalle = new DatosDetalleSubcategoriaProducto(77L, 10L, "Celulares", true);

        Mockito.when(service.detalleSubcategoriaAdmin(id)).thenReturn(detalle);

        mvc.perform(get("/catalogo/admin/subcategorias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.categoriaId").value(10))
                .andExpect(jsonPath("$.nombre").value("Celulares"))
                .andExpect(jsonPath("$.activo").value(true));

        Mockito.verify(service, times(1)).detalleSubcategoriaAdmin(id);
    }

    @Test
    @DisplayName("ADMIN: GET /catalogo/admin/categorias/{id} -> 200 + detalle")
    @WithMockUser(roles = "ADMIN")
    void detalleCategoriaAdmin_ok_200() throws Exception {
        Long id = 10L;
        var detalle = new DatosDetalleCategoriaProducto(10L, "Electrónica", true);

        Mockito.when(service.detalleCategoriaAdmin(id)).thenReturn(detalle);

        mvc.perform(get("/catalogo/admin/categorias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Electrónica"))
                .andExpect(jsonPath("$.activo").value(true));

        Mockito.verify(service, times(1)).detalleCategoriaAdmin(id);
    }

    // ============================
    // ======= SEGURIDAD EXTRA =====
    // ============================

    @Test
    @DisplayName("USER: GET /catalogo/admin/categorias -> 403")
    @WithMockUser(roles = "USER")
    void listarCategorias_user_forbidden_403() throws Exception {
        mvc.perform(get("/catalogo/admin/categorias"))
                .andExpect(status().isForbidden());

        Mockito.verify(service, never()).listarCategoriasAdmin();
    }
}

