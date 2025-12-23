package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.dto.topico.*;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Categoria;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicoServiceTest {

    @Mock TopicoRepository topicoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CursoRepository cursoRepository;
    @Mock RespuestaService respuestaService;

    @InjectMocks TopicoService topicoService;

    private Usuario autor;
    private Usuario admin;
    private Usuario otro;
    private Categoria categoria;
    private Curso curso;
    private Topico topico;

    @BeforeEach
    void setUp() {
        // ======== mocks base (suficientes para que TopicoMapper NO reviente) ========
        autor = mock(Usuario.class);
        when(autor.getId()).thenReturn(10L);
        when(autor.getNombre()).thenReturn("Autor");
        when(autor.esAdmin()).thenReturn(false);

        admin = mock(Usuario.class);
        when(admin.getId()).thenReturn(99L);
        when(admin.getNombre()).thenReturn("Admin");
        when(admin.esAdmin()).thenReturn(true);

        otro = mock(Usuario.class);
        when(otro.getId()).thenReturn(50L);
        when(otro.getNombre()).thenReturn("Otro");
        when(otro.esAdmin()).thenReturn(false);

        categoria = mock(Categoria.class);
        when(categoria.getId()).thenReturn(2L);
        when(categoria.getNombre()).thenReturn("Categoria");

        curso = mock(Curso.class);
        when(curso.getId()).thenReturn(1L);
        when(curso.getNombre()).thenReturn("Curso");
        when(curso.getCategoria()).thenReturn(categoria);

        topico = mock(Topico.class);
        when(topico.getId()).thenReturn(100L);
        when(topico.getTitulo()).thenReturn("Titulo");
        when(topico.getMensaje()).thenReturn("Mensaje");
        when(topico.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(topico.getAutor()).thenReturn(autor);
        when(topico.getCurso()).thenReturn(curso);
        when(topico.getStatus()).thenReturn(StatusTopico.ACTIVO);
    }

    // =========================
    // CREAR
    // =========================
    @Test
    void crearTopico_ok() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", 1L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        // El service hace new Topico(...) y lo guarda.
        // Devolvemos un Topico mock que tenga lo necesario para el mapper.
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

        verify(usuarioRepository).findById(10L);
        verify(cursoRepository).findById(1L);
        verify(topicoRepository).save(any(Topico.class));
    }

    @Test
    void crearTopico_autorNoExiste_404() {
        var datos = new DatosRegistroTopico("Titulo", "Mensaje", 1L);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoService.crearTopico(datos, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Autor");

        verify(topicoRepository, never()).save(any());
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
        var page = new PageImpl<>(List.of(
                new DatosListadoTopico(1L, "T", LocalDateTime.now(), "A", 1L, "C", 2L, "Cat",
                        StatusTopico.ACTIVO, 0L, null)
        ), pageable, 1);

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

        // ✅ IMPORTANTE: Page<DatosListadoRespuesta> (no PageImpl raw)
        Page<DatosListadoRespuesta> respuestasPage =
                new PageImpl<>(List.of(), Pageable.unpaged(), 0);

        when(respuestaService.listarPorTopico(eq(100L), any(Pageable.class)))
                .thenReturn(respuestasPage);

        var dto = topicoService.detallarTopico(100L);

        assertThat(dto).isNotNull();
        verify(topicoRepository).findById(100L);
        verify(respuestaService).listarPorTopico(eq(100L), eq(Pageable.unpaged()));
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
    }

    @Test
    void actualizarTopico_ok_siEsAutor() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(topico.getAutor()).thenReturn(autor);
        when(autor.getId()).thenReturn(10L);

        var datos = new DatosActualizarTopico("Nuevo", "Nuevo msg", null, StatusTopico.ACTIVO);

        var dto = topicoService.actualizarTopico(100L, datos, 10L);

        assertThat(dto).isNotNull();
        verify(topicoRepository).findById(100L);
    }

    // =========================
    // ELIMINAR
    // =========================
    @Test
    void eliminarTopico_ok_siEsAutor() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autor));
        when(topico.getAutor()).thenReturn(autor);
        when(autor.getId()).thenReturn(10L);
        when(autor.esAdmin()).thenReturn(false);

        topicoService.eliminarTopico(100L, 10L);

        verify(topicoRepository).delete(topico);
    }

    @Test
    void eliminarTopico_ok_siEsAdmin() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));

        topicoService.eliminarTopico(100L, 99L);

        verify(topicoRepository).delete(topico);
    }

    @Test
    void eliminarTopico_403_siNoEsAutorNiAdmin() {
        when(topicoRepository.findById(100L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(50L)).thenReturn(Optional.of(otro));
        when(topico.getAutor()).thenReturn(autor);
        when(autor.getId()).thenReturn(10L);

        assertThatThrownBy(() -> topicoService.eliminarTopico(100L, 50L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Solo el autor");

        // ✅ IMPORTANTE: tipar any() para evitar ambiguous delete(...)
        verify(topicoRepository, never()).delete(any(Topico.class));
    }

    // =========================
    // BUSCAR (validación rango fechas)
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
    void buscar_ok_llamaRepo() {
        var filtro = new TopicoFiltro(
                "security", 1L, 10L, StatusTopico.ACTIVO,
                null, null,
                "Curso", "Categoria"
        );

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(
                new DatosListadoTopico(1L, "T", LocalDateTime.now(), "A", 1L, "C", 2L, "Cat",
                        StatusTopico.ACTIVO, 0L, null)
        ), pageable, 1);

        when(topicoRepository.buscarConMetricas(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(page);

        var res = topicoService.buscar(filtro, pageable);

        assertThat(res.getTotalElements()).isEqualTo(1);
        verify(topicoRepository).buscarConMetricas(
                eq("security"),
                eq(1L),
                eq(10L),
                eq(StatusTopico.ACTIVO),
                isNull(),
                isNull(),
                eq("Curso"),
                eq("Categoria"),
                eq(pageable)
        );
    }
}