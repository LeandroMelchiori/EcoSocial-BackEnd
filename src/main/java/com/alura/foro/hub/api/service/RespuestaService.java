// service/RespuestaService.java
package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Regla típica: solo el autor del tópico puede marcar solución (o ADMIN si lo manejás)
        var autorTopicoId = r.getTopico().getAutor().getId();
        if (!autorTopicoId.equals(usuarioId)) {
            throw new IllegalStateException("No tenés permisos para marcar solución");
        }

        var topicoId = r.getTopico().getId();
        respuestaRepository.findByTopicoIdAndSolucionTrue(topicoId)
                .forEach(resp -> resp.setSolucion(false));

        r.setSolucion(true);
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
}
