package com.alura.foro.hub.api.modules.foro.service;

import com.alura.foro.hub.api.modules.foro.dto.topico.*;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.modules.foro.mapper.TopicoMapper;
import com.alura.foro.hub.api.modules.foro.repository.CursoRepository;
import com.alura.foro.hub.api.modules.foro.repository.TopicoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicoService {

    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final RespuestaService respuestaService;

    // Métricas
    private final Counter topicosCreados;
    private final Counter topicosActualizados;
    private final Counter topicosEliminados;
    private final Timer topicoActualizarTimer;

    public TopicoService(TopicoRepository topicoRepository,
                         UsuarioRepository usuarioRepository,
                         CursoRepository cursoRepository,
                         RespuestaService respuestaService,
                         MeterRegistry meterRegistry) {

        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.respuestaService = respuestaService;

        this.topicosCreados = Counter.builder("forohub.topicos.creados")
                .description("Cantidad de tópicos creados")
                .register(meterRegistry);

        this.topicosActualizados = Counter.builder("forohub.topicos.actualizados")
                .description("Cantidad de tópicos actualizados")
                .register(meterRegistry);

        this.topicosEliminados = Counter.builder("forohub.topicos.eliminados")
                .description("Cantidad de tópicos eliminados")
                .register(meterRegistry);

        this.topicoActualizarTimer = Timer.builder("forohub.topicos.actualizar.tiempo")
                .description("Tiempo de actualización de tópico")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    // =========================
    //      CREAR TÓPICO
    // =========================
    @Transactional
    public DatosDetalleTopico crearTopico(DatosRegistroTopico datos, Long usuarioId) {
        var autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado"));

        var curso = cursoRepository.findById(datos.cursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        var topico = new Topico(datos, autor, curso);
        topico = topicoRepository.save(topico);

        // ✅ métrica
        topicosCreados.increment();

        return TopicoMapper.toDetalle(topico, List.of());
    }

    // =========================
    //      LISTAR TÓPICOS
    // =========================
    public Page<DatosListadoTopico> listar(Pageable pageable) {
        return topicoRepository.listarConMetricas(pageable);
    }

    // =========================
    //   DETALLE POR ID
    // =========================
    @Transactional(readOnly = true)
    public DatosDetalleTopico detallarTopico(Long id) {
        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var respuestas = respuestaService
                .listarPorTopico(id, Pageable.unpaged())
                .getContent();

        return TopicoMapper.toDetalle(topico, respuestas);
    }

    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosDetalleTopico actualizarTopico(Long id, DatosActualizarTopico datos, Long usuarioId) {

        // ✅ medimos TODO el flujo de actualización
        return topicoActualizarTimer.record(() -> {

            var topico = topicoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

            if (!topico.getAutor().getId().equals(usuarioId)) {
                throw new ForbiddenException("Solo el autor puede modificar el tópico");
            }

            Curso curso = null;
            if (datos.cursoId() != null) {
                curso = cursoRepository.findById(datos.cursoId())
                        .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
            }

            var fechaCreacion = topico.getFechaCreacion();
            if (fechaCreacion == null) {
                throw new BadRequestException("El tópico no tiene fecha de creación válida");
            }

            Duration duracion = Duration.between(fechaCreacion, LocalDateTime.now());
            if (duracion.toMinutes() > 1440) {
                throw new BadRequestException("El tiempo para editar este topico ya expiró");
            }

            TopicoMapper.aplicarActualizacion(topico, datos, curso);

            // ✅ métrica (si llegó hasta acá, se actualizó)
            topicosActualizados.increment();

            return TopicoMapper.toDetalle(topico);
        });
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminarTopico(Long idTopico, Long userId) {
        var topico = topicoRepository.findById(idTopico)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado"));

        if (!user.esAdmin() && !topico.getAutor().getId().equals(userId)) {
            throw new ForbiddenException("Solo el autor del tópico puede eliminarlo");
        }

        topicoRepository.delete(topico);

        // ✅ métrica
        topicosEliminados.increment();
    }

    // =========================
    //      BUSCAR
    // =========================
    public Page<DatosListadoTopico> buscar(TopicoFiltro filtro, Pageable pageable) {
        String q = normalizar(filtro.q());
        String nombreCurso = normalizar(filtro.nombreCurso());
        String nombreCategoria = normalizar(filtro.nombreCategoria());

        if (filtro.desde() != null && filtro.hasta() != null &&
                filtro.desde().isAfter(filtro.hasta())) {
            throw new BadRequestException(
                    "Rango de fechas inválido: 'desde' no puede ser mayor que 'hasta'."
            );
        }

        return topicoRepository.buscarConMetricas(
                q,
                filtro.cursoId(),
                filtro.autorId(),
                filtro.status(),
                filtro.desde(),
                filtro.hasta(),
                nombreCurso,
                nombreCategoria,
                pageable
        );
    }

    private String normalizar(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isBlank() ? null : s;
    }
}