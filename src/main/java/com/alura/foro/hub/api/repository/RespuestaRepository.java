package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    // Buscar respuestas de un tópico específico
    Page<Respuesta> findByTopicoId(Long topicoId, Pageable pageable);

    // Buscar respuestas marcadas como solución
    List<Respuesta> findBySolucionTrue();

    List<Respuesta> findByTopicoIdAndSolucionTrue(Long topicoId);

    Page<Respuesta> findByTopicoIdOrderByFechaCreacionAsc(Long topicoId, Pageable pageable);

}
