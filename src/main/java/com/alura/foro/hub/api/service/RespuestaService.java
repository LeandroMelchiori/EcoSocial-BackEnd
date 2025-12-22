// service/RespuestaService.java
package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.mapper.RespuestaMapper;
import com.alura.foro.hub.api.repository.*;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;

    public RespuestaService(RespuestaRepository respuestaRepository,
                            TopicoRepository topicoRepository,
                            UsuarioRepository usuarioRepository) {
        this.respuestaRepository = respuestaRepository;
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
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

        var respuesta = RespuestaMapper.fromCrear(datos, topico, autor);
        respuesta = respuestaRepository.save(respuesta);

        return RespuestaMapper.toListado(respuesta);
    }

    @Transactional(readOnly = true)
    public Page<DatosListadoRespuesta> listarPorTopico(Long topicoId, Pageable pageable) {
        topicoRepository.findById(topicoId)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        return respuestaRepository
                .findByTopicoIdOrderBySolucionDescFechaCreacionDesc(topicoId, pageable)
                .map(RespuestaMapper::toListado);
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

        return RespuestaMapper.toListado(respuesta);
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

        RespuestaMapper.aplicarActualizacion(respuesta, dto);

        return RespuestaMapper.toListado(respuesta);
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
}
