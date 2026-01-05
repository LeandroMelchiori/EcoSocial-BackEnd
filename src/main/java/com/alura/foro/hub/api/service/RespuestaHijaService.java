package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuestaHija.DatosActualizarRespuestaHija;
import com.alura.foro.hub.api.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.RespuestaHija;
import com.alura.foro.hub.api.mapper.RespuestaHijaMapper;
import com.alura.foro.hub.api.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.repository.RespuestaRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RespuestaHijaService {

    private final RespuestaHijaRepository respuestaHijaRepository;
    private final RespuestaRepository respuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MeterRegistry meterRegistry;

    private static final String M_RH_ACTIONS = "forohub.respuestas_hijas.acciones";
    private static final String M_RH_TIME = "forohub.respuestas_hijas.tiempo";


    public RespuestaHijaService(RespuestaHijaRepository respuestaHijaRepository,
                                RespuestaRepository respuestaRepository,
                                UsuarioRepository usuarioRepository,
                                MeterRegistry meterRegistry) {
        this.respuestaHijaRepository = respuestaHijaRepository;
        this.respuestaRepository = respuestaRepository;
        this.usuarioRepository = usuarioRepository;
        this.meterRegistry = meterRegistry;
    }

    // =========================
    //      LISTAR
    // =========================
    @Transactional(readOnly = true)
    public List<DatosListadoRespuestaHija> listarPorRespuesta(Long respuestaId) {
        // valida que la respuesta exista (para devolver 404 si no existe)
        respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        return respuestaHijaRepository.buscarPorRespuestaConAutor(respuestaId)
                .stream()
                .map(RespuestaHijaMapper::toListado)
                .toList();
    }

    // =========================
    //      CREAR
    // =========================
    // =========================
    //      CREAR
    // =========================
    @Transactional
    public DatosListadoRespuestaHija crear(Long respuestaId, DatosCrearRespuestaHija dto, Long autorId) {
        try {
            return time(M_RH_TIME, "crear", () -> {

                var respuesta = respuestaRepository.findById(respuestaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

                var topico = respuesta.getTopico();
                if (topico.getStatus() == StatusTopico.CERRADO) {
                    throw new BadRequestException("El tópico está cerrado y no admite respuestas");
                }

                var autor = usuarioRepository.findById(autorId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                var hija = RespuestaHijaMapper.fromCrear(dto, respuesta, autor);
                hija = respuestaHijaRepository.save(hija);

                inc(M_RH_ACTIONS, "crear", "ok");
                return RespuestaHijaMapper.toListado(hija);
            });
        } catch (RuntimeException e) {
            incError(M_RH_ACTIONS, "crear", e.getClass().getSimpleName());
            throw e;
        }
    }


    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosListadoRespuestaHija actualizar(Long hijaId, DatosActualizarRespuestaHija dto, Long userId) {
        try {
            return time(M_RH_TIME, "actualizar", () -> {

                RespuestaHija rh = respuestaHijaRepository.findById(hijaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta hija no encontrada"));

                // tiempo max edición (igual que tu lógica)
                Duration duracion = Duration.between(rh.getFechaCreacion(), LocalDateTime.now());
                if (duracion.toMinutes() > 10) {
                    throw new BadRequestException("El tiempo para editar esta respuesta ya expiró");
                }

                if (!rh.getAutor().getId().equals(userId)) {
                    throw new ForbiddenException("Solo el autor puede editar esta respuesta hija");
                }

                if (rh.getRespuesta().getTopico().getStatus() == StatusTopico.CERRADO) {
                    throw new BadRequestException("El tópico está cerrado y no admite edición de respuestas");
                }

                String nuevoMensaje = dto.mensaje().trim();
                if (!nuevoMensaje.equals(rh.getMensaje())) {
                    rh.setMensaje(nuevoMensaje);
                    rh.setEditado(true);
                }

                inc(M_RH_ACTIONS, "actualizar", "ok");
                return RespuestaHijaMapper.toListado(rh);
            });
        } catch (RuntimeException e) {
            incError(M_RH_ACTIONS, "actualizar", e.getClass().getSimpleName());
            throw e;
        }
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminar(Long hijaId, Long userId) {
        try {
            timeVoid(M_RH_TIME, "eliminar", () -> {

                var user = usuarioRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                var rh = respuestaHijaRepository.findById(hijaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta hija no encontrada"));

                Long autorHijaId = rh.getAutor().getId();
                Long autorTopicoId = rh.getRespuesta().getTopico().getAutor().getId();

                boolean puedeEliminar =
                        autorHijaId.equals(userId)
                                || autorTopicoId.equals(userId)
                                || user.esAdmin();

                if (!puedeEliminar) {
                    throw new ForbiddenException("No tenés permisos para eliminar esta respuesta hija");
                }

                respuestaHijaRepository.delete(rh);
                inc(M_RH_ACTIONS, "eliminar", "ok");
            });
        } catch (RuntimeException e) {
            incError(M_RH_ACTIONS, "eliminar", e.getClass().getSimpleName());
            throw e;
        }
    }


    // =========================
    //      METRICS HELPERS
    // =========================
    private void inc(String name, String accion, String resultado) {
        meterRegistry.counter(name,
                "accion", accion,
                "resultado", resultado
        ).increment();
    }

    private void incError(String name, String accion, String error) {
        meterRegistry.counter(name,
                "accion", accion,
                "resultado", "error",
                "error", error
        ).increment();
    }

    private <T> T time(String name, String accion, java.util.concurrent.Callable<T> callable) {
        Timer timer = Timer.builder(name)
                .publishPercentileHistogram()
                .tag("accion", accion)
                .register(meterRegistry);

        try {
            return timer.recordCallable(callable);
        } catch (RuntimeException e) {
            // mantiene ForbiddenException/BadRequestException tal cual
            throw e;
        } catch (Exception e) {
            // checked exceptions (no debería haber, pero por seguridad)
            throw new RuntimeException(e);
        }
    }


    private void timeVoid(String name, String accion, Runnable runnable) {
        Timer timer = Timer.builder(name)
                .publishPercentileHistogram()
                .tag("accion", accion)
                .register(meterRegistry);

        timer.record(runnable);
    }
}
