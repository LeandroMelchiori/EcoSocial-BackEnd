package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicoService {

    private final TopicoRepository topicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;

    public TopicoService(TopicoRepository topicoRepository,
                         UsuarioRepository usuarioRepository,
                         CursoRepository cursoRepository) {
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
    }

    // =========================
    //      CREAR TÓPICO
    // =========================
    @Transactional
    public Topico crearTopico(DatosRegistroTopico datos, Long userId) {

        Usuario autor = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

        Curso curso = cursoRepository.findById(datos.cursoId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Topico topico = new Topico(datos, autor, curso);

        return topicoRepository.save(topico);
    }


    // =========================
    //      LISTAR TÓPICOS
    // =========================
    public List<DatosListadoTopico> listar() {
        return topicoRepository.findAll()
                .stream()
                .map(t -> new DatosListadoTopico(
                        t.getId(),
                        t.getTitulo(),
                        t.getMensaje(),
                        t.getFechaCreacion(),
                        t.getAutor().getNombre(),
                        t.getCurso().getNombre(),
                        t.getStatus()
                ))
                .toList();
    }

    // =========================
    //   DETALLE POR ID
    // =========================
    public DatosDetalleTopico buscarPorId(Long id) {
        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El tópico no existe"));

        return new DatosDetalleTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor().getNombre(),
                topico.getCurso().getNombre(),
                topico.getStatus()
        );
    }

    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosDetalleTopico actualizar(Long id, DatosActualizarTopico datos) {

        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El tópico no existe"));

        if (datos.titulo() != null) {
            topico.setTitulo(datos.titulo());
        }

        if (datos.mensaje() != null) {
            topico.setMensaje(datos.mensaje());
        }

        if (datos.status() != null) {
            topico.setStatus(datos.status());
        }

        // JPA hace flush al final de la transacción, pero guardar acá no molesta
        topicoRepository.save(topico);

        return new DatosDetalleTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor().getNombre(),
                topico.getCurso().getNombre(),
                topico.getStatus()
        );
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminar(Long id) {
        if (!topicoRepository.existsById(id)) {
            throw new EntityNotFoundException("El tópico no existe");
        }
        topicoRepository.deleteById(id);
    }
}
