package com.alura.foro.hub.api.modules.foro.service;

import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosActualizarRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import com.alura.foro.hub.api.modules.foro.domain.model.RespuestaHija;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.modules.foro.service.RespuestaHijaService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RespuestaHijaServiceTest {

    @Mock private RespuestaHijaRepository respuestaHijaRepository;
    @Mock private RespuestaRepository respuestaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    private RespuestaHijaService service;

    @BeforeEach
    void setup() {
        service = new RespuestaHijaService(
                respuestaHijaRepository,
                respuestaRepository,
                usuarioRepository,
                new SimpleMeterRegistry() // 👈 REAL
        );
    }
    // =========================
    //          CREAR
    // =========================

    @Test
    void crear_ok_cuandoTopicoAbierto_yUsuarioExiste() {
        Long respuestaId = 10L;
        Long autorId = 5L;

        var topico = topico(StatusTopico.ACTIVO);
        var respuesta = respuesta(respuestaId, topico);
        var autor = usuario(autorId, "Sacha");
        var dto = new DatosCrearRespuestaHija("  Hola  ");

        when(respuestaRepository.findById(respuestaId)).thenReturn(Optional.of(respuesta));
        when(usuarioRepository.findById(autorId)).thenReturn(Optional.of(autor));

        ArgumentCaptor<RespuestaHija> captor = ArgumentCaptor.forClass(RespuestaHija.class);

        when(respuestaHijaRepository.save(any(RespuestaHija.class)))
                .thenAnswer(inv -> {
                    RespuestaHija rh = inv.getArgument(0);
                    rh.setId(99L);
                    rh.setFechaCreacion(LocalDateTime.now());
                    rh.setEditado(false);
                    return rh;
                });

        DatosListadoRespuestaHija out = service.crear(respuestaId, dto, autorId);

        verify(respuestaHijaRepository).save(captor.capture());
        RespuestaHija guardada = captor.getValue();

        assertEquals("Hola", guardada.getMensaje());
        assertEquals(respuesta, guardada.getRespuesta());
        assertEquals(autor, guardada.getAutor());

        assertEquals(99L, out.id());
        assertEquals("Hola", out.mensaje());
        assertEquals("Sacha", out.autorNombre());
        assertNotNull(out.fechaCreacion());
        assertFalse(out.editado());
    }

    @Test
    void crear_falla_siRespuestaNoExiste() {
        when(respuestaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.crear(10L, new DatosCrearRespuestaHija("hola"), 1L));
    }

    @Test
    void crear_falla_siTopicoCerrado() {
        var respuesta = respuesta(10L, topico(StatusTopico.CERRADO));
        when(respuestaRepository.findById(10L)).thenReturn(Optional.of(respuesta));

        assertThrows(BadRequestException.class,
                () -> service.crear(10L, new DatosCrearRespuestaHija("hola"), 1L));
    }

    @Test
    void crear_falla_siUsuarioNoExiste() {
        var respuesta = respuesta(10L, topico(StatusTopico.ACTIVO));
        when(respuestaRepository.findById(10L)).thenReturn(Optional.of(respuesta));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.crear(10L, new DatosCrearRespuestaHija("hola"), 5L));
    }

    // =========================
    //          LISTAR
    // =========================

    @Test
    void listarPorRespuesta_ok_devuelveDTOs() {
        Long respuestaId = 10L;

        // si tu listar valida existencia de respuesta (como te propuse), dejalo.
        // si NO valida existencia, podés borrar este stub sin problema.
        when(respuestaRepository.findById(respuestaId))
                .thenReturn(Optional.of(respuesta(respuestaId, topico(StatusTopico.ACTIVO))));

        var autor = usuario(1L, "Ana");
        var rh = new RespuestaHija();
        rh.setId(7L);
        rh.setMensaje("msg");
        rh.setAutor(autor);
        rh.setFechaCreacion(LocalDateTime.now());
        rh.setEditado(false);

        when(respuestaHijaRepository.buscarPorRespuestaConAutor(respuestaId))
                .thenReturn(List.of(rh));

        var out = service.listarPorRespuesta(respuestaId);

        assertEquals(1, out.size());
        assertEquals(7L, out.get(0).id());
        assertEquals("msg", out.get(0).mensaje());
        assertEquals("Ana", out.get(0).autorNombre());
    }

    // =========================
    //        ACTUALIZAR
    // =========================

    @Test
    void actualizar_ok_siEsAutor_yDentroDe10Min_yTopicoAbierto() {
        Long hijaId = 20L;
        Long userId = 5L;

        var topico = topico(StatusTopico.ACTIVO);
        var respuesta = respuesta(10L, topico);
        var autor = usuario(userId, "Autor");

        var rh = new RespuestaHija();
        rh.setId(hijaId);
        rh.setAutor(autor);
        rh.setRespuesta(respuesta);
        rh.setMensaje("Viejo");
        rh.setFechaCreacion(LocalDateTime.now().minusMinutes(5));
        rh.setEditado(false);

        when(respuestaHijaRepository.findById(hijaId)).thenReturn(Optional.of(rh));

        var dto = new DatosActualizarRespuestaHija("  Nuevo  ");
        var out = service.actualizar(hijaId, dto, userId);

        assertEquals("Nuevo", rh.getMensaje());
        assertTrue(rh.getEditado());
        assertEquals(hijaId, out.id());
        assertEquals("Nuevo", out.mensaje());
    }

    @Test
    void actualizar_falla_siNoEsAutor() {
        Long hijaId = 20L;

        var rh = respuestaHijaBasica(
                hijaId,
                usuario(99L, "Otro"),
                respuesta(10L, topico(StatusTopico.ACTIVO)),
                LocalDateTime.now().minusMinutes(1)
        );

        when(respuestaHijaRepository.findById(hijaId)).thenReturn(Optional.of(rh));

        assertThrows(ForbiddenException.class,
                () -> service.actualizar(hijaId, new DatosActualizarRespuestaHija("x"), 5L));
    }

    @Test
    void actualizar_falla_siPasaronMasDe10Min() {
        Long hijaId = 20L;
        Long userId = 5L;

        var rh = respuestaHijaBasica(
                hijaId,
                usuario(userId, "Autor"),
                respuesta(10L, topico(StatusTopico.ACTIVO)),
                LocalDateTime.now().minusMinutes(11)
        );

        when(respuestaHijaRepository.findById(hijaId)).thenReturn(Optional.of(rh));

        assertThrows(BadRequestException.class,
                () -> service.actualizar(hijaId, new DatosActualizarRespuestaHija("x"), userId));
    }

    @Test
    void actualizar_falla_siTopicoCerrado() {
        Long hijaId = 20L;
        Long userId = 5L;

        var rh = respuestaHijaBasica(
                hijaId,
                usuario(userId, "Autor"),
                respuesta(10L, topico(StatusTopico.CERRADO)),
                LocalDateTime.now().minusMinutes(1)
        );

        when(respuestaHijaRepository.findById(hijaId)).thenReturn(Optional.of(rh));

        assertThrows(BadRequestException.class,
                () -> service.actualizar(hijaId, new DatosActualizarRespuestaHija("x"), userId));
    }

    // =========================
    // Helpers
    // =========================

    private Topico topico(StatusTopico status) {
        Topico t = new Topico();
        t.setId(1L);
        t.setStatus(status);
        return t;
    }

    private Respuesta respuesta(Long id, Topico topico) {
        Respuesta r = new Respuesta();
        r.setId(id);
        r.setTopico(topico);
        return r;
    }

    private Usuario usuario(Long id, String nombre) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        return u;
    }

    private RespuestaHija respuestaHijaBasica(Long hijaId, Usuario autor, Respuesta respuesta, LocalDateTime fechaCreacion) {
        RespuestaHija rh = new RespuestaHija();
        rh.setId(hijaId);
        rh.setAutor(autor);
        rh.setRespuesta(respuesta);
        rh.setMensaje("msg");
        rh.setFechaCreacion(fechaCreacion);
        rh.setEditado(false);
        return rh;
    }
}
