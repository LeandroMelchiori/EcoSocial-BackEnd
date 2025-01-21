package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.Topico;
import com.alura.foro.hub.api.repository.TopicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TopicoService {

    private final TopicoRepository topicoRepository;

    public TopicoService(TopicoRepository topicoRepository) {
        this.topicoRepository = topicoRepository;
    }

    public List<Topico> listarTopicos() {
        return topicoRepository.findAll();
    }

    public Optional<Topico> obtenerPorId(Long id) {
        return topicoRepository.findById(id);
    }

    @Transactional
    public Topico crearTopico(Topico topico) {
        return topicoRepository.save(topico);
    }

    @Transactional
    public Topico actualizarTopico(Long id, Topico topicoActualizado) {
        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tópico no encontrado"));
        topico.setTitulo(topicoActualizado.getTitulo());
        topico.setMensaje(topicoActualizado.getMensaje());
        topico.setCategoria(topicoActualizado.getCategoria());
        return topicoRepository.save(topico);
    }

    @Transactional
    public void eliminarTopico(Long id) {
        if (!topicoRepository.existsById(id)) {
            throw new IllegalArgumentException("El tópico no existe.");
        }
        topicoRepository.deleteById(id);
    }
}
