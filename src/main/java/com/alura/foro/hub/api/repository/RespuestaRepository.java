package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Topico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    // Buscar respuestas de un tópico específico
    List<Respuesta> findByTopico(Topico topico);

    // Buscar respuestas marcadas como solución
    List<Respuesta> findBySolucionTrue();
}
