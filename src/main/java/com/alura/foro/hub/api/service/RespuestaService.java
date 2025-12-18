// service/RespuestaService.java
package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.mapper.RespuestaMapper;
import com.alura.foro.hub.api.repository.*;
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
    public DatosListadoRespuesta crear(DatosCrearRespuesta datos, Long userId) {
        var topico = topicoRepository.findById(datos.topicoId())
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var autor = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (topico.getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite respuestas");
        }

        var r = new Respuesta();
        r.setMensaje(datos.mensaje());
        r.setTopico(topico);
        r.setAutor(autor);

        r = respuestaRepository.save(r);

        return toDTO(r);
    }

    public Page<DatosListadoRespuesta> listarPorTopico(Long topicoId, Pageable pageable) {
        topicoRepository.findById(topicoId)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        return respuestaRepository
                .findByTopicoIdOrderBySolucionDescFechaCreacionDesc(topicoId, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public DatosListadoRespuesta marcarSolucion(Long respuestaId, Long usuarioId) {
        var respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        var autorTopicoId = respuesta.getTopico().getAutor().getId();
        if (!autorTopicoId.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tenés permisos para marcar solución");
        }

        var topico = respuesta.getTopico();

        // ✅ Desmarca soluciones anteriores y marca la actual
        respuestaRepository.desmarcarSoluciones(topico.getId());
        respuesta.setSolucion(true);
        respuestaRepository.save(respuesta);

        // ✅ Marca el topico como solucionado
        topico.solucionado();
        topicoRepository.save(topico);
        return toDTO(respuesta);
    }

    @Transactional
    public DatosListadoRespuesta actualizar(Long respuestaId,
                                            DatosActualizarRespuesta datos,
                                            Long usuarioId) {

        Respuesta respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        // 🔒 Solo el autor puede editar
        if (!respuesta.getAutor().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor puede editar la respuesta");
        }

        if (respuesta.getTopico().getStatus() == StatusTopico.CERRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tópico está cerrado y no admite edición de respuestas");
        }

        respuesta.setMensaje(datos.mensaje());

        return toDTO(respuesta);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permisos para eliminar esta respuesta");
        }
        if (Boolean.TRUE.equals(r.getSolucion())) {
            var topico = r.getTopico();
            topico.reactivarTopico(); //
            topicoRepository.save(topico);
        }
        respuestaRepository.delete(r);
    }

    private DatosListadoRespuesta toDTO(Respuesta r) {
        return RespuestaMapper.toListado(r);
    }
}
