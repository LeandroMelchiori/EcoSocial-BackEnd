package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByCategoriaId(Long categoriaId);
}
