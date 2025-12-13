package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.repository.RespuestaRepository;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
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
    private final RespuestaService respuestaService;

    public TopicoService(TopicoRepository topicoRepository,
                         UsuarioRepository usuarioRepository,
                         CursoRepository cursoRepository,
                         RespuestaService respuestaService) {
        this.topicoRepository = topicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.respuestaService = respuestaService;
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
        return topicoRepository.listarConMetricas();
    }


    // =========================
    //   DETALLE POR ID
    // =========================
    @Transactional(readOnly = true)
    public DatosDetalleTopico detallarTopico(Long id) {

        Topico topico = topicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var respuestas = respuestaService
                .listarPorTopico(id, Pageable.unpaged())
                .getContent();

        return new DatosDetalleTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor().getNombre(),
                topico.getCurso().getNombre(),
                topico.getStatus(),
                respuestas
        );
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
    public void eliminarTopico(Long idTopico, Usuario usuarioLogueado) {
        var topico = topicoRepository.findById(idTopico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tópico no encontrado"));

        boolean esAdmin = usuarioLogueado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 💣 Solo el autor o admin pueden borrar
        if (!esAdmin && !topico.getAutor().getId().equals(usuarioLogueado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor del tópico puede eliminarlo");
        }

        // Borrado físico
        topicoRepository.delete(topico);
    }

    private DatosListadoRespuesta toDTORespuesta(Respuesta r) {
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
