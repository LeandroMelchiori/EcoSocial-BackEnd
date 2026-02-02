package com.alura.foro.hub.api.modules.security;

import com.alura.foro.hub.api.modules.foro.controller.CursoController;
import com.alura.foro.hub.api.modules.foro.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.security.config.SecurityConfigurations;
import com.alura.foro.hub.api.security.filter.RateLimitFilter;
import com.alura.foro.hub.api.security.filter.SecurityFilter;
import com.alura.foro.hub.api.modules.foro.service.CursoService;
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


@WebMvcTest(CursoController.class)
@Import(SecurityConfigurations.class)
class CursoControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean CursoService cursoService;

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
        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Docker", "categoriaId": 2 }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cursoService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void crear_conUser_403() throws Exception {
        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Docker", "categoriaId": 2 }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cursoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_conAdmin_201() throws Exception {
        when(cursoService.crear(any()))
                .thenReturn(new DatosListadoCurso(3L, "Docker", 2L, "Cloud"));

        mvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Docker", "categoriaId": 2 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/cursos/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nombre").value("Docker"));

        verify(cursoService).crear(any());
    }
}
