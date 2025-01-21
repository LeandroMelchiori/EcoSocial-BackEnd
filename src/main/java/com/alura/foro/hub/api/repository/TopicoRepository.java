package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Topico;
import com.alura.foro.hub.api.domain.EstadoTopico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicoRepository extends JpaRepository<Topico, Long> {

    // Buscar tópicos por autor
    List<Topico> findByAutorUsername(String username);

    // Buscar tópicos por estado
    List<Topico> findByEstado(EstadoTopico estado);

    // Buscar tópicos por título con búsqueda parcial
    List<Topico> findByTituloContainingIgnoreCase(String titulo);
}
