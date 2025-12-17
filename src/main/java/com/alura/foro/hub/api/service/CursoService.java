package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;

@Service
public class CursoService {

    private final CategoriaRepository categoriaRepository;
    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository,
                        CategoriaRepository categoriaRepository) {
        this.cursoRepository = cursoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<DatosListadoCurso> listar(Long categoriaId) {
        var cursos = (categoriaId == null)
                ? cursoRepository.findAll()
                : cursoRepository.findByCategoriaId(categoriaId);

        return cursos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DatosListadoCurso detallar(Long id) {
        var c = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
        return toDTO(c);
    }

    private DatosListadoCurso toDTO(Curso c) {
        return new DatosListadoCurso(
                c.getId(),
                c.getNombre(),
                c.getCategoria().getId(),
                c.getCategoria().getNombre()
        );
    }

    @Transactional
    public DatosListadoCurso crear(DatosCrearCurso datos) {
        var categoria = categoriaRepository.findById(datos.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        var curso = new Curso();
        curso.setNombre(datos.nombre().trim());
        curso.setCategoria(categoria);

        curso = cursoRepository.save(curso);
        return toDTO(curso);
    }

    @Transactional
    public DatosListadoCurso actualizar(Long id, DatosActualizarCurso datos) {
        var curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        var categoria = categoriaRepository.findById(datos.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        curso.setNombre(datos.nombre().trim());
        curso.setCategoria(categoria);

        return toDTO(curso);
    }

    @Transactional
    public void eliminar(Long id) {
        var curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        // Si querés, acá podrías bloquear si tiene tópicos.
        cursoRepository.delete(curso);
    }
}
