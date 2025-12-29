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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RespuestaHijaService {

    private final RespuestaHijaRepository respuestaHijaRepository;
    private final RespuestaRepository respuestaRepository;
    private final UsuarioRepository usuarioRepository;

    public RespuestaHijaService(RespuestaHijaRepository respuestaHijaRepository,
                                RespuestaRepository respuestaRepository,
                                UsuarioRepository usuarioRepository) {
        this.respuestaHijaRepository = respuestaHijaRepository;
        this.respuestaRepository = respuestaRepository;
        this.usuarioRepository = usuarioRepository;
    }

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


    @Transactional
    public DatosListadoRespuestaHija crear(Long respuestaId, DatosCrearRespuestaHija dto, Long autorId) {
        var respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        var topico = respuesta.getTopico();
        if (topico.getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite respuestas");
        }

        var autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        var hija = RespuestaHijaMapper.fromCrear(dto, respuesta, autor);
        hija = respuestaHijaRepository.save(hija);

        return RespuestaHijaMapper.toListado(hija);
    }

    @Transactional
    public DatosListadoRespuestaHija actualizar(Long hijaId, DatosActualizarRespuestaHija dto, Long userId) {
        RespuestaHija rh = respuestaHijaRepository.findById(hijaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta hija no encontrada"));

        Duration duracion = Duration.between(rh.getFechaCreacion(), LocalDateTime.now());

        if (duracion.toMinutes() > 10) {throw new BadRequestException("El tiempo para editar esta respuesta ya expiró");}

        if (!rh.getAutor().getId().equals(userId)) {
            throw new ForbiddenException("Solo el autor puede editar esta respuesta hija");
        }

        if (rh.getRespuesta().getTopico().getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite edición de respuestas");
        }

        rh.setMensaje(dto.mensaje().trim());

        return RespuestaHijaMapper.toListado(rh);
    }

    @Transactional
    public void eliminar(Long hijaId, Long userId) {
        var user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        RespuestaHija rh = respuestaHijaRepository.findById(hijaId)
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

        if (rh.getRespuesta().getTopico().getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tópico está cerrado y no admite eliminar respuestas");
        }

        respuestaHijaRepository.delete(rh);
    }

}
