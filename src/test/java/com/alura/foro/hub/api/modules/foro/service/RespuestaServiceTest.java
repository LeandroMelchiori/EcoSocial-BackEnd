package com.alura.foro.hub.api.modules.foro.service;

import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.modules.foro.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.user.domain.Perfil;
import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaRepository;
import com.alura.foro.hub.api.modules.foro.repository.TopicoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.modules.foro.service.RespuestaService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestaServiceTest {

    @Mock
    RespuestaRepository respuestaRepository;

    @Mock
    TopicoRepository topicoRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    RespuestaHijaRepository respuestaHijaRepository;

    RespuestaService service;

    Usuario autor;
    Usuario autorTopico;
    Topico topico;
    Respuesta respuesta;

    @BeforeEach
    void setup() {
        service = new RespuestaService(
                respuestaRepository,
                topicoRepository,
                usuarioRepository,
                respuestaHijaRepository,
                new SimpleMeterRegistry()
        );

        autor = new Usuario();
        autor.setId(1L);
        autor.setNombre("Autor");

        autorTopico = new Usuario();
        autorTopico.setId(2L);
        autorTopico.setNombre("Autor Topico");

        topico = new Topico();
        topico.setId(10L);
        topico.setAutor(autorTopico);
        topico.setStatus(StatusTopico.ACTIVO);

        respuesta = new Respuesta();
        respuesta.setId(100L);
        respuesta.setMensaje("Mensaje");
        respuesta.setAutor(autor);
        respuesta.setTopico(topico);
        respuesta.setSolucion(false);
        respuesta.setFechaCreacion(LocalDateTime.now());
    }

    // ─────────────────────────────
    // CREAR
    // ─────────────────────────────
    @Test
    void crear_ok() {
        var dto = new DatosCrearRespuesta(10L, "Respuesta nueva");

        when(topicoRepository.findById(10L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(respuestaRepository.save(any())).thenAnswer(inv -> {
            Respuesta r = inv.getArgument(0);
            r.setId(999L);
            return r;
        });

        DatosListadoRespuesta result = service.crear(dto, 1L);

        assertThat(result.mensaje()).isEqualTo("Respuesta nueva");
        assertThat(result.autorNombre()).isEqualTo("Autor");
    }

    @Test
    void crear_topicoCerrado_lanzaException() {
        topico.setStatus(StatusTopico.CERRADO);
        when(topicoRepository.findById(10L)).thenReturn(Optional.of(topico));

        var dto = new DatosCrearRespuesta(10L, "x");

        assertThatThrownBy(() -> service.crear(dto, 1L))
                .isInstanceOf(BadRequestException.class);
    }

    // ─────────────────────────────
    // LISTAR
    // ─────────────────────────────
    @Test
    void listarPorTopico_ok() {
        when(topicoRepository.findById(10L)).thenReturn(Optional.of(topico));

        // para mapCantidadHijas(topicoId)
        when(respuestaRepository.contarHijasPorRespuestaDeTopico(10L))
                .thenReturn(java.util.Collections.singletonList(new Object[]{100L, 0L}));

        Page<Respuesta> page = new PageImpl<>(List.of(respuesta));
        when(respuestaRepository
                .findByTopicoIdOrderBySolucionDescFechaCreacionDesc(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        Page<DatosListadoRespuesta> result =
                service.listarPorTopico(10L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).autorNombre()).isEqualTo("Autor");
    }

    // ─────────────────────────────
    // MARCAR SOLUCIÓN
    // ─────────────────────────────
    @Test
    void marcarSolucion_ok() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        service.marcarSolucion(100L, autorTopico.getId());

        assertThat(respuesta.getSolucion()).isTrue();
        verify(respuestaRepository).desmarcarSoluciones(10L);
        verify(topicoRepository).save(topico);
    }

    @Test
    void marcarSolucion_usuarioNoAutorTopico_lanzaForbidden() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        assertThatThrownBy(() -> service.marcarSolucion(100L, 999L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ─────────────────────────────
    // ACTUALIZAR
    // ─────────────────────────────
    @Test
    void actualizar_ok() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        var dto = new DatosActualizarRespuesta("Mensaje editado");

        DatosListadoRespuesta result =
                service.actualizar(100L, dto, autor.getId());

        assertThat(result.mensaje()).isEqualTo("Mensaje editado");
    }

    @Test
    void actualizar_noAutor_lanzaForbidden() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        var dto = new DatosActualizarRespuesta("x");

        assertThatThrownBy(() -> service.actualizar(100L, dto, 999L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ─────────────────────────────
    // ELIMINAR
    // ─────────────────────────────
    @Test
    void eliminar_ok_porAutorRespuesta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        service.eliminar(100L, 1L);

        verify(respuestaRepository).delete(respuesta);
    }

    @Test
    void eliminar_sinPermisos_lanzaForbidden() {
        Usuario otro = new Usuario();
        otro.setId(99L);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(otro));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        assertThatThrownBy(() -> service.eliminar(100L, 99L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void eliminar_ok_siEsAdmin() {
        Usuario admin = new Usuario();
        admin.setId(50L);

        Perfil perfilAdmin = new Perfil();
        perfilAdmin.setNombre("ADMIN");
        admin.getPerfiles().add(perfilAdmin);

        when(usuarioRepository.findById(50L)).thenReturn(Optional.of(admin));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        service.eliminar(100L, 50L);

        verify(respuestaRepository).delete(respuesta);
    }
}

