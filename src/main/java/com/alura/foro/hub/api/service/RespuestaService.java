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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespuestaHijaRepository respuestaHijaRepository;

    public RespuestaService(RespuestaRepository respuestaRepository,
                            TopicoRepository topicoRepository,
                            UsuarioRepository usuarioRepository,
                            RespuestaHijaRepository respuestaHijaRepository) {
        this.respuestaRepository = respuestaRepository;
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.respuestaHijaRepository = respuestaHijaRepository;
    }


    @Transactional
    public DatosListadoRespuesta crear(DatosCrearRespuesta datos, Long autorId) {
        var topico = topicoRepository.findById(datos.topicoId())
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        if (topico.getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite respuestas");
        }

        var autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var respuesta = respuestaRepository.save(
                RespuestaMapper.fromCrear(datos, topico, autor)
        );

        return RespuestaMapper.toListado(respuesta, 0L);
    }

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

    @Transactional(readOnly = true)
    public DatosDetalleRespuesta detalle(Long respuestaId) {
        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        Duration duracion = Duration.between(respuesta.getFechaCreacion(), LocalDateTime.now());

        if (duracion.toMinutes() > 720) {throw new BadRequestException("El tiempo para editar esta respuesta ya expiró");}

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




    @Transactional
    public DatosListadoRespuesta marcarSolucion(Long respuestaId, Long usuarioId) {
        var respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        var topico = respuesta.getTopico();

        if (!topico.getAutor().getId().equals(usuarioId)) {
            throw new ForbiddenException("No tenés permisos para marcar solución");
        }

        // desmarca anteriores y marca esta
        respuestaRepository.desmarcarSoluciones(topico.getId());
        respuesta.setSolucion(true);
        respuestaRepository.save(respuesta);

        topico.solucionado();
        topicoRepository.save(topico);

        return RespuestaMapper.toListado(respuesta, 0L);
    }

    @Transactional
    public DatosListadoRespuesta actualizar(Long respuestaId, DatosActualizarRespuesta dto, Long usuarioId) {
        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        if (!respuesta.getAutor().getId().equals(usuarioId)) {
            throw new ForbiddenException("Solo el autor puede editar la respuesta");
        }

        if (respuesta.getTopico().getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite edición de respuestas");
        }

        Duration duracion = Duration.between(respuesta.getFechaCreacion(), LocalDateTime.now());

        if (duracion.toMinutes() > 10) {throw new BadRequestException("El tiempo para editar esta respuesta ya expiró");}

        RespuestaMapper.aplicarActualizacion(respuesta, dto);

        return RespuestaMapper.toListado(respuesta, 0L);
    }

    @Transactional
    public void eliminar(Long respuestaId, Long userId) {
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
    }

    private Map<Long, Long> mapCantidadHijas(Long topicoId) {
        return respuestaRepository.contarHijasPorRespuestaDeTopico(topicoId)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

}
