package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    @Transactional(readOnly = true)
    public DatosDetalleTopico obtenerDetalle(Long id) {
        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tópico no encontrado"));

        return new DatosDetalleTopico(topico);
    }

    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosDetalleTopico actualizarTopico(Long idTopico,
                                               DatosActualizarTopico datos,
                                               Long usuarioIdLogueado) {

        var topico = topicoRepository.findById(idTopico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tópico no encontrado"));

        // 💣 Solo el autor puede modificar
        if (!topico.getAutor().getId().equals(usuarioIdLogueado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor del tópico puede modificarlo");
        }

        // 🔁 Actualizamos solo lo que venga distinto de null / vacío
        if (datos.titulo() != null && !datos.titulo().isBlank()) {
            topico.setTitulo(datos.titulo());
        }

        if (datos.mensaje() != null && !datos.mensaje().isBlank()) {
            topico.setMensaje(datos.mensaje());
        }

        if (datos.cursoId() != null) {
            var curso = cursoRepository.findById(datos.cursoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));
            topico.setCurso(curso);
        }

        if (datos.status() != null) {
            topico.setStatus(datos.status());
        }

        return new DatosDetalleTopico(topico);
    }


    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminarTopico(Long idTopico, Long usuarioIdLogueado) {
        var topico = topicoRepository.findById(idTopico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tópico no encontrado"));

        // 💣 Solo el autor puede borrar
        if (!topico.getAutor().getId().equals(usuarioIdLogueado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor del tópico puede eliminarlo");
        }

        // Borrado físico
        topicoRepository.delete(topico);

        // (Si más adelante querés borrado lógico, acá cambiamos a:
        // topico.setStatus(StatusTopico.ELIMINADO);
        // y listo)
    }
}
