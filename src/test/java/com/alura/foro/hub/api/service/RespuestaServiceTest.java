package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.repository.RespuestaRepository;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestaServiceTest {

    @Mock RespuestaRepository respuestaRepository;
    @Mock TopicoRepository topicoRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks RespuestaService respuestaService;

    private Usuario autorTopico;
    private Usuario autorRespuesta;
    private Usuario admin;
    private Topico topicoAbierto;
    private Topico topicoCerrado;
    private Respuesta respuesta;

    @BeforeEach
    void setUp() {
        autorTopico = mock(Usuario.class);
        when(autorTopico.getId()).thenReturn(10L);
        when(autorTopico.getNombre()).thenReturn("AutorTopico");
        when(autorTopico.esAdmin()).thenReturn(false);

        autorRespuesta = mock(Usuario.class);
        when(autorRespuesta.getId()).thenReturn(20L);
        when(autorRespuesta.getNombre()).thenReturn("AutorRespuesta");
        when(autorRespuesta.esAdmin()).thenReturn(false);

        admin = mock(Usuario.class);
        when(admin.getId()).thenReturn(99L);
        when(admin.getNombre()).thenReturn("Admin");
        when(admin.esAdmin()).thenReturn(true);

        topicoAbierto = mock(Topico.class);
        when(topicoAbierto.getId()).thenReturn(1L);
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(topicoAbierto.getStatus()).thenReturn(StatusTopico.ACTIVO);

        topicoCerrado = mock(Topico.class);
        when(topicoCerrado.getId()).thenReturn(2L);
        when(topicoCerrado.getAutor()).thenReturn(autorTopico);
        when(topicoCerrado.getStatus()).thenReturn(StatusTopico.CERRADO);

        respuesta = mock(Respuesta.class);
        when(respuesta.getId()).thenReturn(100L);
        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(respuesta.getMensaje()).thenReturn("Mensaje");
        when(respuesta.getSolucion()).thenReturn(false);
        when(respuesta.getFechaCreacion()).thenReturn(LocalDateTime.now());
    }

    // =========================
    // CREAR
    // =========================

    @Test
    void crear_ok() {
        var dto = new DatosCrearRespuesta(1L, "  Hola  ");

        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topicoAbierto));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(autorRespuesta));

        // save devuelve la entidad “persistida”
        var guardada = mock(Respuesta.class);
        when(guardada.getId()).thenReturn(101L);
        when(guardada.getMensaje()).thenReturn("Hola");
        when(guardada.getAutor()).thenReturn(autorRespuesta);
        when(guardada.getSolucion()).thenReturn(false);
        when(guardada.getFechaCreacion()).thenReturn(LocalDateTime.now());

        when(respuestaRepository.save(any(Respuesta.class))).thenReturn(guardada);

        DatosListadoRespuesta res = respuestaService.crear(dto, 20L);

        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo(101L);
        assertThat(res.autorNombre()).isEqualTo("AutorRespuesta");
        assertThat(res.solucion()).isFalse();

        verify(topicoRepository).findById(1L);
        verify(usuarioRepository).findById(20L);
        verify(respuestaRepository).save(any(Respuesta.class));
    }

    @Test
    void crear_topicoNoExiste_404() {
        var dto = new DatosCrearRespuesta(1L, "Hola");
        when(topicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.crear(dto, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tópico");

        verify(respuestaRepository, never()).save(any());
    }

    @Test
    void crear_topicoCerrado_400() {
        var dto = new DatosCrearRespuesta(2L, "Hola");
        when(topicoRepository.findById(2L)).thenReturn(Optional.of(topicoCerrado));

        assertThatThrownBy(() -> respuestaService.crear(dto, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(usuarioRepository, never()).findById(anyLong());
        verify(respuestaRepository, never()).save(any());
    }

    @Test
    void crear_autorNoExiste_404() {
        var dto = new DatosCrearRespuesta(1L, "Hola");
        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topicoAbierto));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.crear(dto, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario");

        verify(respuestaRepository, never()).save(any());
    }

    // =========================
    // LISTAR POR TOPICO
    // =========================

    @Test
    void listarPorTopico_ok() {
        when(topicoRepository.findById(1L)).thenReturn(Optional.of(topicoAbierto));

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(respuesta), pageable, 1);

        when(respuestaRepository.findByTopicoIdOrderBySolucionDescFechaCreacionDesc(1L, pageable))
                .thenReturn(page);

        var res = respuestaService.listarPorTopico(1L, pageable);

        assertThat(res.getTotalElements()).isEqualTo(1);
        verify(topicoRepository).findById(1L);
        verify(respuestaRepository).findByTopicoIdOrderBySolucionDescFechaCreacionDesc(1L, pageable);
    }

    @Test
    void listarPorTopico_topicoNoExiste_404() {
        when(topicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.listarPorTopico(1L, PageRequest.of(0, 10)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tópico");

        verify(respuestaRepository, never()).findByTopicoIdOrderBySolucionDescFechaCreacionDesc(anyLong(), any());
    }

    // =========================
    // MARCAR SOLUCION
    // =========================

    @Test
    void marcarSolucion_ok_siAutorDelTopico() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        DatosListadoRespuesta dto = respuestaService.marcarSolucion(100L, 10L);

        assertThat(dto).isNotNull();

        verify(respuestaRepository).desmarcarSoluciones(1L);
        verify(respuesta).setSolucion(true);
        verify(respuestaRepository).save(respuesta);
        verify(topicoAbierto).solucionado();
        verify(topicoRepository).save(topicoAbierto);
    }

    @Test
    void marcarSolucion_403_siNoEsAutorDelTopico() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        assertThatThrownBy(() -> respuestaService.marcarSolucion(100L, 999L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("marcar solución");

        verify(respuestaRepository, never()).desmarcarSoluciones(anyLong());
        verify(respuestaRepository, never()).save(any());
        verify(topicoRepository, never()).save(any());
    }

    @Test
    void marcarSolucion_respuestaNoExiste_404() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.marcarSolucion(100L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Respuesta");

        verify(respuestaRepository, never()).desmarcarSoluciones(anyLong());
    }

    // =========================
    // ACTUALIZAR
    // =========================

    @Test
    void actualizar_ok_siAutorRespuesta() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));
        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);
        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(topicoAbierto.getStatus()).thenReturn(StatusTopico.ACTIVO);

        var dto = new DatosActualizarRespuesta("  Nuevo mensaje  ");

        DatosListadoRespuesta res = respuestaService.actualizar(100L, dto, 20L);

        assertThat(res).isNotNull();
        verify(respuesta).setMensaje("Nuevo mensaje");
    }

    @Test
    void actualizar_403_siNoEsAutorRespuesta() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));
        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        var dto = new DatosActualizarRespuesta("Nuevo");

        assertThatThrownBy(() -> respuestaService.actualizar(100L, dto, 999L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Solo el autor");

        verify(respuesta, never()).setMensaje(anyString());
    }

    @Test
    void actualizar_400_siTopicoCerrado() {
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));
        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoCerrado);
        when(topicoCerrado.getStatus()).thenReturn(StatusTopico.CERRADO);

        var dto = new DatosActualizarRespuesta("Nuevo");

        assertThatThrownBy(() -> respuestaService.actualizar(100L, dto, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verify(respuesta, never()).setMensaje(anyString());
    }

    // =========================
    // ELIMINAR
    // =========================

    @Test
    void eliminar_ok_siAutorRespuesta() {
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(autorRespuesta));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        when(respuesta.getSolucion()).thenReturn(false);

        respuestaService.eliminar(100L, 20L);

        verify(respuestaRepository).delete(respuesta);
        verify(topicoRepository, never()).save(any());
    }

    @Test
    void eliminar_ok_siAutorTopico() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(autorTopico));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        when(respuesta.getSolucion()).thenReturn(false);

        respuestaService.eliminar(100L, 10L);

        verify(respuestaRepository).delete(respuesta);
    }

    @Test
    void eliminar_ok_siAdmin() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(admin));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        when(respuesta.getSolucion()).thenReturn(false);

        respuestaService.eliminar(100L, 99L);

        verify(respuestaRepository).delete(respuesta);
    }

    @Test
    void eliminar_403_siNoTienePermiso() {
        var random = mock(Usuario.class);
        when(random.getId()).thenReturn(77L);
        when(random.esAdmin()).thenReturn(false);

        when(usuarioRepository.findById(77L)).thenReturn(Optional.of(random));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(topicoAbierto.getAutor()).thenReturn(autorTopico);
        when(autorTopico.getId()).thenReturn(10L);

        assertThatThrownBy(() -> respuestaService.eliminar(100L, 77L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("eliminar");

        verify(respuestaRepository, never()).delete(any());
    }

    @Test
    void eliminar_siEraSolucion_reactivaTopico() {
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(autorRespuesta));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.of(respuesta));

        when(respuesta.getAutor()).thenReturn(autorRespuesta);
        when(autorRespuesta.getId()).thenReturn(20L);

        when(respuesta.getTopico()).thenReturn(topicoAbierto);
        when(respuesta.getSolucion()).thenReturn(true);

        respuestaService.eliminar(100L, 20L);

        verify(topicoAbierto).reactivarTopico();
        verify(topicoRepository).save(topicoAbierto);
        verify(respuestaRepository).delete(respuesta);
    }

    @Test
    void eliminar_usuarioNoExiste_404() {
        when(usuarioRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.eliminar(100L, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario");

        verify(respuestaRepository, never()).delete(any());
    }

    @Test
    void eliminar_respuestaNoExiste_404() {
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(autorRespuesta));
        when(respuestaRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestaService.eliminar(100L, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Respuesta");

        verify(respuestaRepository, never()).delete(any());
    }
}
