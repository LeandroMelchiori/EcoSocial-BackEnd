package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Buscar categoría por nombre
    Categoria findByNombre(String nombre);

    // Verificar si una categoría existe
    boolean existsByNombre(String nombre);
}
