package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
