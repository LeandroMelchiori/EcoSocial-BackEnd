// service/RespuestaService.java
package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
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
    public DatosListadoRespuesta crear(DatosCrearRespuesta datos, Long autorId) {
        var topico = topicoRepository.findById(datos.topicoId())
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (topico.getStatus() == StatusTopico.CERRADO) {
            throw new IllegalStateException("El tópico está cerrado y no admite respuestas");
        }

        var r = new Respuesta();
        r.setMensaje(datos.mensaje());
        r.setTopico(topico);
        r.setAutor(autor);

        r = respuestaRepository.save(r);

        return toDTO(r);
    }

    public Page<DatosListadoRespuesta> listarPorTopico(Long topicoId, Pageable pageable) {
        return respuestaRepository.findByTopicoId(topicoId, pageable).map(this::toDTO);
    }

    @Transactional
    public DatosListadoRespuesta marcarSolucion(Long respuestaId, Long usuarioId) {
        var r = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        var autorTopicoId = r.getTopico().getAutor().getId();
        if (!autorTopicoId.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tenés permisos para marcar solución");
        }

        var topico = r.getTopico();

        respuestaRepository.desmarcarSoluciones(topico.getId());
        r.setSolucion(true);

        // opcional (pero re recomendado)
        topico.setStatus(StatusTopico.CERRADO);

        return toDTO(r);
    }


    private DatosListadoRespuesta toDTO(Respuesta r) {
        return new DatosListadoRespuesta(
                r.getId(),
                r.getTopico().getId(),
                r.getMensaje(),
                r.getFechaCreacion(),
                r.getAutor().getId(),
                r.getAutor().getNombre(),
                r.getSolucion()
        );
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

        respuesta.setMensaje(datos.mensaje());

        return toDTO(respuesta);
    }

    @Transactional
    public void eliminar(Long respuestaId, Long usuarioId) {

        var r = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada"));

        Long autorRespuestaId = r.getAutor().getId();
        Long autorTopicoId = r.getTopico().getAutor().getId();

        boolean puedeEliminar = autorRespuestaId.equals(usuarioId) || autorTopicoId.equals(usuarioId);
        if (!puedeEliminar) {
            throw new IllegalStateException("No tenés permisos para eliminar esta respuesta");
        }

        respuestaRepository.delete(r);
    }
}
