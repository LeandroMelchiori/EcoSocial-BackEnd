package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.modules.foro.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.modules.foro.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.modules.foro.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.modules.foro.dto.topico.TopicoFiltro;
import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.repository.CursoRepository;
import com.alura.foro.hub.api.modules.foro.repository.TopicoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.modules.foro.service.RespuestaService;
import com.alura.foro.hub.api.modules.foro.service.TopicoService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicoServiceTest {

    @Mock TopicoRepository topicoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CursoRepository cursoRepository;
    @Mock
    RespuestaService respuestaService;

    TopicoService topicoService;
    MeterRegistry meterRegistry;

    private Usuario autor;
    private Usuario admin;
    private Usuario otro;
    private Categoria categoria;
    private Curso curso;
    private Topico topico;

    @BeforeEach
    void setUp() {
        autor = mock(Usuario.class);
        admin = mock(Usuario.class);
        otro  = mock(Usuario.class);

        categoria = mock(Categoria.class);
        curso     = mock(Curso.class);

        topico    = mock(Topico.class);
        meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();

        topicoService = new TopicoService(
                topicoRepository,
                usuarioRepository,
                cursoRepository,
                respuestaService,
                meterRegistry
        );
    }

    // =========================
    // CREAR
    // =========================
    @Test
    void crearTopico_ok() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", 1L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        Categoria categoria = mock(Categoria.class);
        when(categoria.getId()).thenReturn(2L);
        when(categoria.getNombre()).thenReturn("Categoria");

        when(curso.getId()).thenReturn(1L);
        when(curso.getNombre()).thenReturn("Curso");
        when(curso.getCategoria()).thenReturn(categoria);

        Topico guardado = mock(Topico.class);
        when(guardado.getId()).thenReturn(200L);
        when(guardado.getTitulo()).thenReturn("Titulo");
        when(guardado.getMensaje()).thenReturn("Mensaje");
        when(guardado.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(guardado.getAutor()).thenReturn(autor);
        when(guardado.getCurso()).thenReturn(curso);
        when(guardado.getStatus()).thenReturn(StatusTopico.ACTIVO);

        when(topicoRepository.save(any(Topico.class))).thenReturn(guardado);

        var dto = topicoService.crearTopico(datos, 10L);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(200L);
    }


    @Test
    void crearTopico_autorNoExiste_404() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", 1L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.crearTopico(datos, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Autor");

        verify(topicoRepository, never()).save(any());
        verify(cursoRepository, never()).findById(anyLong());
    }

    @Test
    void crearTopico_cursoNoExiste_404() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", 1L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(cursoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.crearTopico(datos, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Curso");

        verify(topicoRepository, never()).save(any());
    }

    // =========================
    // LISTAR
    // =========================
    @Test
    void listar_ok() {
        var pageable = PageRequest.of(0, 10);

        var page = new PageImpl<>(
                List.of(new DatosListadoTopico(
                        1L, "T", LocalDateTime.now(),
                        "A", 1L, "C", 2L, "Cat",
                        StatusTopico.ACTIVO,
                        0L, null, null
                )),
                pageable,
                1
        );

        when(topicoRepository.listarConMetricas(pageable)).thenReturn(page);

        var res = topicoService.listar(pageable);

        assertThat(res.getTotalElements()).isEqualTo(1);
        verify(topicoRepository).listarConMetricas(pageable);
    }

    // =========================
    // DETALLAR
    // =========================
    @Test
    void detallarTopico_ok() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));

        when(topico.getId()).thenReturn(100L);
        when(topico.getTitulo()).thenReturn("Titulo");
        when(topico.getMensaje()).thenReturn("Mensaje");
        when(topico.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(topico.getStatus()).thenReturn(StatusTopico.ACTIVO);

        when(topico.getAutor()).thenReturn(autor);

        when(topico.getCurso()).thenReturn(curso);
        when(curso.getId()).thenReturn(1L);

        when(curso.getCategoria()).thenReturn(categoria);
        when(categoria.getId()).thenReturn(2L);

        when(respuestaService.listarPorTopico(eq(100L), eq(Pageable.unpaged())))
                .thenReturn(Page.empty());

        var dto = topicoService.detallarTopico(100L);

        assertThat(dto).isNotNull();

        verify(topicoRepository).findById(100L);
        verify(respuestaService).listarPorTopico(100L, Pageable.unpaged());
    }


    @Test
    void detallarTopico_noExiste_404() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.detallarTopico(100L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tópico");

        verify(respuestaService, never()).listarPorTopico(anyLong(), any());
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Test
    void actualizarTopico_siNoEsAutor_403() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));

        when(topico.getAutor()).thenReturn(autor);
        when(autor.getId()).thenReturn(10L);

        var datos = new DatosActualizarTopico("Nuevo", "Nuevo msg", 1L, StatusTopico.ACTIVO);

        assertThatThrownBy(() -> topicoService.actualizarTopico(100L, datos, 999L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Solo el autor");

        verify(cursoRepository, never()).findById(anyLong());
        verify(topicoRepository).findById(100L);
    }

    @Test
    void actualizarTopico_ok_siEsAutor_y_noCambiaCurso() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));

        when(topico.getAutor()).thenReturn(autor);
        when(topico.getId()).thenReturn(100L);
        when(topico.getTitulo()).thenReturn("Viejo");
        when(topico.getMensaje()).thenReturn("Viejo msg");
        when(topico.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(topico.getStatus()).thenReturn(StatusTopico.ACTIVO);
        when(topico.getCurso()).thenReturn(curso);

        when(autor.getNombre()).thenReturn("Autor");
        when(autor.getId()).thenReturn(10L);

        when(curso.getId()).thenReturn(1L);
        when(curso.getNombre()).thenReturn("Curso");
        when(curso.getCategoria()).thenReturn(categoria);

        when(categoria.getId()).thenReturn(2L);
        when(categoria.getNombre()).thenReturn("Categoria");

        var datos = new DatosActualizarTopico("Nuevo", "Nuevo msg", null, StatusTopico.ACTIVO);

        var dto = topicoService.actualizarTopico(100L, datos, 10L);

        assertThat(dto).isNotNull();
        verify(topicoRepository).findById(100L);
        verify(cursoRepository, never()).findById(anyLong());
    }

    @Test
    void actualizarTopico_cambiaCurso_y_cursoNoExiste_404() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());
        when(topico.getAutor()).thenReturn(autor);
        when(autor.getId()).thenReturn(10L);

        var datos = new DatosActualizarTopico("Nuevo", "Nuevo msg", 99L, StatusTopico.ACTIVO);

        assertThatThrownBy(() -> topicoService.actualizarTopico(100L, datos, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Curso");

        verify(cursoRepository).findById(99L);
    }

    // =========================
    // ELIMINAR
    // =========================
    @Test
    void eliminarTopico_ok_siEsAutor() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autor));

        when(autor.esAdmin()).thenReturn(false);
        when(autor.getId()).thenReturn(10L);

        when(topico.getAutor()).thenReturn(autor);

        topicoService.eliminarTopico(100L, 10L);

        verify(topicoRepository).delete(topico);
    }

    @Test
    void eliminarTopico_ok_siEsAdmin() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        when(admin.esAdmin()).thenReturn(true);

        topicoService.eliminarTopico(100L, 99L);

        verify(topicoRepository).delete(topico);
    }

    @Test
    void eliminarTopico_403_siNoEsAutorNiAdmin() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(50L)).thenReturn(Optional.of(otro));

        when(otro.esAdmin()).thenReturn(false);

        when(topico.getAutor()).thenReturn(autor);

        when(autor.getId()).thenReturn(10L);

        assertThatThrownBy(() -> topicoService.eliminarTopico(100L, 50L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Solo el autor");

        verify(topicoRepository, never()).delete(any(Topico.class));
    }

    @Test
    void eliminarTopico_topicoNoExiste_404() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.eliminarTopico(100L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tópico");

        verify(usuarioRepository, never()).findById(anyLong());
        verify(topicoRepository, never()).delete(any(Topico.class));
    }

    @Test
    void eliminarTopico_usuarioNoExiste_404() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.eliminarTopico(100L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Autor");

        verify(topicoRepository, never()).delete(any(Topico.class));
    }

    // =========================
    // BUSCAR
    // =========================
    @Test
    void buscar_badRequest_siDesdeMayorQueHasta() {
        var desde = LocalDateTime.of(2025, 12, 10, 10, 0);
        var hasta = LocalDateTime.of(2025, 12, 1, 10, 0);

        var filtro = new TopicoFiltro(
                "q", 1L, 2L, StatusTopico.ACTIVO,
                desde, hasta,
                null, null
        );

        assertThatThrownBy(() -> topicoService.buscar(filtro, PageRequest.of(0, 10)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Rango de fechas inválido");

        verify(topicoRepository, never()).buscarConMetricas(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void buscar_ok_normaliza_y_llamaRepo() {
        var filtro = new TopicoFiltro(
                "   security   ", 1L, 10L, StatusTopico.ACTIVO,
                null, null,
                "  Curso  ", "  Categoria "
        );

        var pageable = PageRequest.of(0, 10);

        var page = new PageImpl<>(
                List.of(new DatosListadoTopico(
                        1L, "T", LocalDateTime.now(),
                        "A", 1L, "C", 2L, "Cat",
                        StatusTopico.ACTIVO,
                        0L, null, null
                )),
                pageable,
                1
        );

        when(topicoRepository.buscarConMetricas(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(page);

        var res = topicoService.buscar(filtro, pageable);

        assertThat(res.getTotalElements()).isEqualTo(1);

        verify(topicoRepository).buscarConMetricas(
                eq("security"),      // trim
                eq(1L),
                eq(10L),
                eq(StatusTopico.ACTIVO),
                isNull(),
                isNull(),
                eq("Curso"),         // trim
                eq("Categoria"),     // trim
                eq(pageable)
        );
    }

    @Test
    void buscar_normaliza_blanks_a_null() {
        var filtro = new TopicoFiltro(
                "   ", null, null, null,
                null, null,
                "   ", "   "
        );

        var pageable = PageRequest.of(0, 10);
        when(topicoRepository.buscarConMetricas(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        var res = topicoService.buscar(filtro, pageable);

        assertThat(res).isNotNull();
        verify(topicoRepository).buscarConMetricas(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                isNull(), isNull(),
                eq(pageable)
        );
    }
}
