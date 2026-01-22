package com.alura.foro.hub.api.modules.foro.security;

import com.alura.foro.hub.api.modules.foro.controller.CategoriaController;
import com.alura.foro.hub.api.modules.foro.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.security.config.SecurityConfigurations;
import com.alura.foro.hub.api.security.filter.RateLimitFilter;
import com.alura.foro.hub.api.security.filter.SecurityFilter;
import com.alura.foro.hub.api.modules.foro.service.CategoriaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CategoriaController.class)
@Import(SecurityConfigurations.class)
class CategoriaControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    CategoriaService categoriaService;

    // Mockeamos tus filtros custom para que NO exijan JWT real en tests
    @MockitoBean SecurityFilter securityFilter;
    @MockitoBean RateLimitFilter rateLimitFilter;

    @BeforeEach
    void passthroughFilters() throws Exception {

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(securityFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void crear_sinAuth_401() throws Exception {
        mvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Docker", "categoriaId": 2 }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void crear_conUser_403() throws Exception {
        mvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "AWS" }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conAdmin_201() throws Exception {
        when(categoriaService.crear(any()))
                .thenReturn(new DatosListadoCategoria(3L, "Docker"));

        mvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Docker" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categorias/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Docker"));

        verify(categoriaService).crear(any());
    }
}
