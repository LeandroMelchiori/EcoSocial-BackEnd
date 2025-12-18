package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.mapper.CursoMapper;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.repository.CategoriaRepository;
import com.alura.foro.hub.api.security.exception.BusinessException;
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

        return cursos.stream()
                .map(CursoMapper::toListado)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatosListadoCurso detallar(Long id) {
        var curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
        return CursoMapper.toListado(curso);
    }

    @Transactional
    public DatosListadoCurso crear(DatosCrearCurso datos) {

        var categoria = categoriaRepository.findById(datos.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        if (cursoRepository.existsByNombreIgnoreCase(datos.nombre())) {
            throw new BusinessException("Ya existe un curso con ese nombre");
        }

        var curso = CursoMapper.fromCrear(datos, categoria);
        cursoRepository.save(curso);

        return CursoMapper.toListado(curso);
    }


    @Transactional
    public DatosListadoCurso actualizar(Long id, DatosActualizarCurso datos) {
        var curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        var categoria = categoriaRepository.findById(datos.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        if (cursoRepository.existsByNombreIgnoreCase(datos.nombre())) {
            throw new BusinessException("Ya existe un curso con ese nombre");
        }
        curso.setNombre(datos.nombre().trim());
        curso.setCategoria(categoria);

        return CursoMapper.toListado(curso);
    }

    @Transactional
    public void eliminar(Long id) {
        var curso = cursoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));

        // Si querés, acá podrías bloquear si tiene tópicos.
        cursoRepository.delete(curso);
    }
}
