package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.mapper.TopicoMapper;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.security.exception.BusinessException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
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
    public DatosDetalleTopico crearTopico(DatosRegistroTopico datos, Long usuarioId) {
        var autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado"));

        var curso = cursoRepository.findById(datos.cursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        var topico = new Topico(datos, autor, curso);
        topico = topicoRepository.save(topico);

        return TopicoMapper.toDetalle(topico, List.of());
    }



    // =========================
    //      LISTAR TÓPICOS
    // =========================
    public Page<DatosListadoTopico> listar(Pageable pageable) {
        return topicoRepository.listarConMetricas(pageable);
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

        return TopicoMapper.toDetalle(topico, respuestas);
    }
    // =========================
    //      ACTUALIZAR
    // =========================
    @Transactional
    public DatosDetalleTopico actualizarTopico(Long id, DatosActualizarTopico datos, Long usuarioId) {

        var topico = topicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        if (!topico.getAutor().getId().equals(usuarioId)) {
            throw new ForbiddenException("Solo el autor puede modificar el tópico");
        }

        Curso curso = null;
        if (datos.cursoId() != null) {
            curso = cursoRepository.findById(datos.cursoId())
                    .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
        }

        TopicoMapper.aplicarActualizacion(topico, datos, curso);

        return TopicoMapper.toDetalle(topico);
    }

    // =========================
    //      ELIMINAR
    // =========================
    @Transactional
    public void eliminarTopico(Long idTopico, Long userId) {
        var topico = topicoRepository.findById(idTopico)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        var user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado"));

        if (!user.esAdmin() && !topico.getAutor().getId().equals(userId)) {
            throw new ForbiddenException("Solo el autor del tópico puede eliminarlo");
        }

        // Borrado físico
        topicoRepository.delete(topico);
    }
}