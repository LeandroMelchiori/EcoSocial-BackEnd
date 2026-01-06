// service/RespuestaService.java
package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosDetalleRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.mapper.RespuestaHijaMapper;
import com.alura.foro.hub.api.mapper.RespuestaMapper;
import com.alura.foro.hub.api.repository.*;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaHijaRepository respuestaHijaRepository;
    private final MeterRegistry meterRegistry;
    private static final String M_RESPUESTA_ACTIONS = "forohub.respuestas.acciones";
    private static final String M_RESPUESTA_TIME = "forohub.respuestas.tiempo";


    public RespuestaService(RespuestaRepository respuestaRepository,
                            TopicoRepository topicoRepository,
                            UsuarioRepository usuarioRepository,
                            RespuestaHijaRepository respuestaHijaRepository,
                            MeterRegistry meterRegistry) {
        this.respuestaRepository = respuestaRepository;
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.respuestaHijaRepository = respuestaHijaRepository;
        this.meterRegistry = meterRegistry;
    }

    // =========================
    //      CREAR
    // =========================
    @Transactional
    public DatosListadoRespuesta crear(DatosCrearRespuesta datos, Long autorId) {
        try {
            return time(M_RESPUESTA_TIME, "crear", () -> {
                var topico = topicoRepository.findById(datos.topicoId())
                        .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

                if (topico.getStatus() == StatusTopico.CERRADO) {
                    throw new BadRequestException("El tópico está cerrado y no admite respuestas");
                }

                var autor = usuarioRepository.findById(autorId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                var respuesta = respuestaRepository.save(
                        RespuestaMapper.fromCrear(datos, topico, autor)
                );

                inc(M_RESPUESTA_ACTIONS, "crear", "ok");
                return RespuestaMapper.toListado(respuesta, 0L);
            });
        } catch (RuntimeException e) {
            incError(M_RESPUESTA_ACTIONS, "crear", e.getClass().getSimpleName());
            throw e;
        } catch (Exception e) {
            incError(M_RESPUESTA_ACTIONS, "crear", e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    // =========================
    //      LISTAR
    // =========================
    @Transactional(readOnly = true)
    public Page<DatosListadoRespuesta> listarPorTopico(Long topicoId, Pageable pageable) {
        topicoRepository.findById(topicoId)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        // 1) Traés el conteo una sola vez
        Map<Long, Long> conteos = mapCantidadHijas(topicoId);

        // 2) Listás respuestas SIN cargar hijas
        return respuestaRepository
                .findByTopicoIdOrderBySolucionDescFechaCreacionDesc(topicoId, pageable)
                .map(r -> RespuestaMapper.toListado(
                        r,
                        conteos.getOrDefault(r.getId(), 0L)
                ));
    }

    // =========================
    //      DETALLE LISTADO
    // =========================
    @Transactional(readOnly = true)
    public DatosDetalleRespuesta detalle(Long respuestaId) {
        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));
        
        // hijas con autor (sin N+1)
        var hijas = respuestaHijaRepository.buscarPorRespuestaConAutor(respuestaId)
                .stream()
                .map(RespuestaHijaMapper::toListado)
                .toList();

        return RespuestaMapper.toDetalle(
                respuesta,
                (long) hijas.size(),
                hijas
        );
    }

    // =========================
    //      SOLUCION
    // =========================
    @Transactional
    public DatosListadoRespuesta marcarSolucion(Long respuestaId, Long usuarioId) {
        try {
            return time(M_RESPUESTA_TIME, "marcar_solucion", () -> {
                var respuesta = respuestaRepository.findById(respuestaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

                var topico = respuesta.getTopico();
                if (!topico.getAutor().getId().equals(usuarioId)) {
                    throw new ForbiddenException("No tenés permisos para marcar solución");
                }

                respuestaRepository.desmarcarSoluciones(topico.getId());
                respuesta.setSolucion(true);
                respuestaRepository.save(respuesta);

                topico.solucionado();
                topicoRepository.save(topico);

                inc(M_RESPUESTA_ACTIONS, "marcar_solucion", "ok");
                return RespuestaMapper.toListado(respuesta, 0L);
            });
        } catch (RuntimeException e) {
            incError(M_RESPUESTA_ACTIONS, "marcar_solucion", e.getClass().getSimpleName());
            throw e;
        }
    }

    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosListadoRespuesta actualizar(Long respuestaId, DatosActualizarRespuesta dto, Long usuarioId) {
        try {
            return time(M_RESPUESTA_TIME, "actualizar", () -> {
                Respuesta respuesta = respuestaRepository.findById(respuestaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

                if (!respuesta.getAutor().getId().equals(usuarioId)) {
                    throw new ForbiddenException("Solo el autor puede editar la respuesta");
                }

                if (respuesta.getTopico().getStatus() == StatusTopico.CERRADO) {
                    throw new BadRequestException("El tópico está cerrado y no admite edición");
                }

                Duration duracion = Duration.between(respuesta.getFechaCreacion(), LocalDateTime.now());
                if (duracion.toMinutes() > 10) {
                    throw new BadRequestException("El tiempo para editar esta respuesta ya expiró");
                }

                RespuestaMapper.aplicarActualizacion(respuesta, dto);

                inc(M_RESPUESTA_ACTIONS, "actualizar", "ok");
                return RespuestaMapper.toListado(respuesta, 0L);
            });
        } catch (RuntimeException e) {
            incError(M_RESPUESTA_ACTIONS, "actualizar", e.getClass().getSimpleName());
            throw e;
        }
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminar(Long respuestaId, Long userId) {
        try {
            timeVoid(M_RESPUESTA_TIME, "eliminar", () -> {
                var user = usuarioRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                var r = respuestaRepository.findById(respuestaId)
                        .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

                Long autorRespuestaId = r.getAutor().getId();
                Long autorTopicoId = r.getTopico().getAutor().getId();

                boolean puedeEliminar =
                        autorRespuestaId.equals(userId)
                                || autorTopicoId.equals(userId)
                                || user.esAdmin();

                if (!puedeEliminar) {
                    throw new ForbiddenException("No tenés permisos para eliminar esta respuesta");
                }

                if (Boolean.TRUE.equals(r.getSolucion())) {
                    var topico = r.getTopico();
                    topico.reactivarTopico();
                    topicoRepository.save(topico);
                }

                respuestaRepository.delete(r);
                inc(M_RESPUESTA_ACTIONS, "eliminar", "ok");
            });
        } catch (RuntimeException e) {
            incError(M_RESPUESTA_ACTIONS, "eliminar", e.getClass().getSimpleName());
            throw e;
        }
    }

    // =========================
    //      HELPERS
    // =========================
    private Map<Long, Long> mapCantidadHijas(Long topicoId) {
        return respuestaRepository.contarHijasPorRespuestaDeTopico(topicoId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
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
            // IMPORTANTÍSIMO: preserva ForbiddenException, BadRequestException, etc.
            throw e;
        } catch (Exception e) {
            // Solo por si aparece una checked exception
            throw new RuntimeException(e);
        }
    }

    private void timeVoid(String name, String accion, Runnable runnable) {
        Timer timer = Timer.builder(name)
                .publishPercentileHistogram()
                .tag("accion", accion)
                .register(meterRegistry);

        try {
            timer.record(runnable);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
