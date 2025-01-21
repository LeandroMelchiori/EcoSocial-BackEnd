package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TopicoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    private final TopicoRepository topicoRepository;

    public TopicoService(TopicoRepository topicoRepository) {
        this.topicoRepository = topicoRepository;
    }

    public List<DatosListadoTopico> listarTopicos() {
        return topicoRepository.findAll().stream()
                .map(t -> new DatosListadoTopico(t.getId(), t.getTitulo(), t.getMensaje(), t.getFechaCreacion()))
                .toList();
    }

    public Optional<DatosDetalleTopico> obtenerPorId(Long id) {
        return topicoRepository.findById(id)
                .map(t -> new DatosDetalleTopico(t.getId(), t.getTitulo(), t.getMensaje(), t.getFechaCreacion(),
                        t.getAutor().getNombre(), t.getCurso().getNombre()
                        , t.getEstado()));
    }

    @Transactional
    public Topico crearTopico(DatosRegistroTopico datos) {
        // Obtener el autor desde la base de datos
        Usuario autor = usuarioRepository.findById(datos.autorId())
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado"));

        // Obtener el curso desde la base de datos
        Curso curso = cursoRepository.findById(datos.cursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        // Crear un nuevo objeto Topico y asignar los valores
        Topico topico = new Topico();
        topico.setTitulo(datos.titulo());
        topico.setMensaje(datos.mensaje());
        topico.setEstado(datos.estado());
        topico.setAutor(autor);
        topico.setCurso(curso);

        return topicoRepository.save(topico);
    }

    @Transactional
    public Topico actualizarTopico(Long id, DatosActualizarTopico datos) {
        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tópico no encontrado"));
        topico.setTitulo(datos.titulo());
        topico.setMensaje(datos.mensaje());
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
