package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.Curso;
import com.alura.foro.hub.api.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    @Autowired
    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> obtenerTodosLosCursos() {
        return cursoRepository.findAll();
    }

    public Optional<Curso> obtenerCursoPorId(Long id) {
        return cursoRepository.findById(id);
    }

    public Curso crearCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    public Curso actualizarCurso(Long id, Curso cursoActualizado) {
        if (cursoRepository.existsById(id)) {
            cursoActualizado.setId(id);
            return cursoRepository.save(cursoActualizado);
        }
        return null;
    }

    public void eliminarCurso(Long id) {
        cursoRepository.deleteById(id);
    }


}
