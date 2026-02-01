package com.alura.foro.hub.api.modules.catalogo.unit.producto;

import com.alura.foro.hub.api.modules.catalogo.controller.ProductoController;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.user.domain.Perfil;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductoController.class)
@AutoConfigureMockMvc(addFilters = false) // <- apaga Security filters
@TestPropertySource(properties = "catalogo.storage=minio")
class ProductoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean ProductoService productoService;
    @MockitoBean private CurrentUserService currentUserService;

    // -------------------------
    // Helpers
    // -------------------------
    private Usuario user(Long id) {
        Usuario u = new Usuario();
        u.setId(id);

        u.setNombre("User");
        u.setApellido("Test");
        u.setDni(String.format("%08d", id));
        u.setEmail("u" + id + "@mail.com");
        u.setPassword("x");

        u.setPerfiles(List.of());
        return u;
    }

    @SuppressWarnings("unused")
    private Usuario admin(Long id) {
        Usuario u = user(id);
        Perfil p = new Perfil();
        p.setNombre("ADMIN");
        u.setPerfiles(List.of(p));
        return u;
    }

    private Authentication auth(Usuario u) {
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    private RequestPostProcessor withAuth(Authentication a) {
        return request -> {
            request.setUserPrincipal(a);
            return request;
        };
    }

    private DatosDetalleProducto detalleMock() {
        return new DatosDetalleProducto(
                10L,
                1L, // emprendimientoId (NO usuarioId)
                2L,
                3L,
                "Sillón",
                "Desc",
                true,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                List.of(
                        new DatosImagenProducto(101L, 1, "http://img1"),
                        new DatosImagenProducto(102L, 2, "http://img2")
                )
        );
    }

    // -------------------------
    // POST /catalogo/productos (multipart)
    // -------------------------
    @Test
    void crear_ok_201_y_body() throws Exception {

        var dataPart = new MockMultipartFile(
                "data",
                "data.json",
                "application/json",
                """
                {
                  "categoriaCatalogoId": 2,
                  "subCategoriaCatalogoId": 3,
                  "titulo": "Sillón",
                  "descripcion": "Descripcion valida"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );


        var img1 = new MockMultipartFile(
                "imagenes", "a.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);
        given(productoService.crear(any(DatosCrearProducto.class), anyList(), eq(1L)))
                .willReturn(detalleMock());

        mvc.perform(multipart("/catalogo/productos")
                        .file(dataPart)
                        .file(img1)
                        .with(withAuth(auth(user(1L))))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/catalogo/productos/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.emprendimientoId").value(1))
                .andExpect(jsonPath("$.categoriaId").value(2))
                .andExpect(jsonPath("$.subcategoriaId").value(3))
                .andExpect(jsonPath("$.imagenes.length()").value(2));

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).crear(any(DatosCrearProducto.class), anyList(), eq(1L));
    }

    // -------------------------
    // GET /catalogo/productos/{id}
    // -------------------------
    @Test
    void detalle_ok_200() throws Exception {
        given(productoService.detalle(10L)).willReturn(detalleMock());

        mvc.perform(get("/catalogo/productos/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.imagenes.length()").value(2));

        verify(productoService).detalle(10L);
    }

    // -------------------------
    // GET /catalogo/productos (listar)
    // -------------------------
    @Test
    void listar_ok_200() throws Exception {
        given(productoService.listar(any(), any(), any(), any()))
                .willAnswer(inv -> org.springframework.data.domain.Page.empty());

        mvc.perform(get("/catalogo/productos")
                        .param("categoriaId", "2")
                        .param("subcategoriaId", "3")
                        .param("q", "sillon")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk());

        verify(productoService).listar(eq(2L), eq(3L), eq("sillon"), any());
    }

    // -------------------------
    // DELETE /catalogo/productos/{id}
    // -------------------------
    @Test
    void eliminar_ok_204() throws Exception {
        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);

        mvc.perform(delete("/catalogo/productos/{id}", 10L)
                        .with(withAuth(auth(user(1L))))
                )
                .andExpect(status().isNoContent());

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).eliminar(10L, 1L);
    }

    // -------------------------
    // PUT /catalogo/productos/{id} (multipart)
    // -------------------------
    @Test
    void actualizar_ok_200() throws Exception {
        var dataPart = new MockMultipartFile(
                "data",
                "data.json",
                "application/json",
                """
                {
                  "categoriaCatalogoId": 2,
                  "subCategoriaCatalogoId": 3,
                  "titulo": "Nuevo",
                  "descripcion": "Nueva desc"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        var img1 = new MockMultipartFile(
                "imagenes", "a.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);
        given(productoService.actualizar(eq(10L), any(DatosActualizarProducto.class), anyList(), eq(1L)))
                .willReturn(detalleMock());

        mvc.perform(multipart("/catalogo/productos/{id}", 10L)
                        .file(dataPart)
                        .file(img1)
                        .with(withAuth(auth(user(1L))))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).actualizar(eq(10L), any(DatosActualizarProducto.class), anyList(), eq(1L));
    }

    // -------------------------
    // PUT /catalogo/productos/{id}/imagenes/orden
    // -------------------------
    @Test
    void reordenar_ok_200() throws Exception {
        var body = new DatosReordenarImagenes(List.of(101L, 102L));

        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);
        given(productoService.reordenarImagenes(eq(10L), any(DatosReordenarImagenes.class), eq(1L)))
                .willReturn(detalleMock());

        mvc.perform(put("/catalogo/productos/{id}/imagenes/orden", 10L)
                        .with(withAuth(auth(user(1L))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).reordenarImagenes(eq(10L), any(DatosReordenarImagenes.class), eq(1L));
    }

    // -------------------------
    // DELETE /catalogo/productos/{productoId}/imagenes/{imagenId}
    // -------------------------
    @Test
    void eliminarImagen_ok_204() throws Exception {
        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);

        mvc.perform(delete("/catalogo/productos/{pid}/imagenes/{iid}", 10L, 101L)
                        .with(withAuth(auth(user(1L))))
                )
                .andExpect(status().isNoContent());

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).eliminarImagen(10L, 101L, 1L);
    }

    // -------------------------
    // PUT /catalogo/productos/{productoId}/imagenes/{imagenId} (multipart)
    // -------------------------
    @Test
    void reemplazarImagen_ok_200() throws Exception {
        var img = new MockMultipartFile(
                "imagen", "n.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);
        given(productoService.reemplazarImagen(eq(10L), eq(101L), any(), eq(1L)))
                .willReturn(detalleMock());

        mvc.perform(multipart("/catalogo/productos/{pid}/imagenes/{iid}", 10L, 101L)
                        .file(img)
                        .with(withAuth(auth(user(1L))))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).reemplazarImagen(eq(10L), eq(101L), any(), eq(1L));
    }

    // -------------------------
    // POST /catalogo/productos/{productoId}/imagenes (multipart)
    // -------------------------
    @Test
    void agregarImagenes_ok_200() throws Exception {
        var img1 = new MockMultipartFile(
                "imagenes", "a.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );
        var img2 = new MockMultipartFile(
                "imagenes", "b.png", "image/png", "y".getBytes(StandardCharsets.UTF_8)
        );

        given(currentUserService.userId(any(Authentication.class))).willReturn(1L);
        given(productoService.agregarImagenes(eq(10L), anyList(), eq(1L)))
                .willReturn(detalleMock());

        mvc.perform(multipart("/catalogo/productos/{pid}/imagenes", 10L)
                        .file(img1)
                        .file(img2)
                        .with(withAuth(auth(user(1L))))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(currentUserService).userId(any(Authentication.class));
        verify(productoService).agregarImagenes(eq(10L), anyList(), eq(1L));
    }

    @Test
    void crear_falla_400_si_falta_data_part() throws Exception {
        var img1 = new MockMultipartFile(
                "imagenes", "a.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        mvc.perform(multipart("/catalogo/productos")
                        .file(img1)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
        verifyNoInteractions(currentUserService);
    }

    @Test
    void crear_falla_400_si_data_no_es_json() throws Exception {
        var dataPart = new MockMultipartFile(
                "data", "data.json", "application/json",
                "esto-no-es-json".getBytes(StandardCharsets.UTF_8)
        );

        mvc.perform(multipart("/catalogo/productos")
                        .file(dataPart)
                        .with(withAuth(auth(user(1L))))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void actualizar_falla_400_si_falta_data_part() throws Exception {
        var img1 = new MockMultipartFile(
                "imagenes", "a.png", "image/png", "x".getBytes(StandardCharsets.UTF_8)
        );

        mvc.perform(multipart("/catalogo/productos/10")
                        .file(img1)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .with(withAuth(auth(user(1L))))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }
}
