package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Topico;
import com.alura.foro.hub.api.repository.RespuestaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;

    public RespuestaService(RespuestaRepository respuestaRepository) {
        this.respuestaRepository = respuestaRepository;
    }

    public List<Respuesta> listarRespuestas() {
        return respuestaRepository.findAll();
    }

    public List<Respuesta> obtenerPorTopico(Topico topico) {
        return respuestaRepository.findByTopico(topico);
    }

    public Optional<Respuesta> obtenerPorId(Long id) {
        return respuestaRepository.findById(id);
    }

    @Transactional
    public Respuesta crearRespuesta(Respuesta respuesta) {
        return respuestaRepository.save(respuesta);
    }

    @Transactional
    public Respuesta actualizarRespuesta(Long id, Respuesta respuestaActualizada) {
        Respuesta respuesta = respuestaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada"));
        respuesta.setMensaje(respuestaActualizada.getMensaje());
        respuesta.setSolucion(respuestaActualizada.getSolucion());
        return respuestaRepository.save(respuesta);
    }

    @Transactional
    public void eliminarRespuesta(Long id) {
        if (!respuestaRepository.existsById(id)) {
            throw new IllegalArgumentException("La respuesta no existe.");
        }
        respuestaRepository.deleteById(id);
    }
}
