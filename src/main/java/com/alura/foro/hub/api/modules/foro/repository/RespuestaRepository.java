package com.alura.foro.hub.api.modules.foro.repository;

import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    // Buscar respuestas marcadas como solución
    List<Respuesta> findBySolucionTrue();

    List<Respuesta> findByTopicoIdAndSolucionTrue(Long topicoId);

    @Modifying
    @Query("""
        update Respuesta r
        set r.solucion = false
        where r.topico.id = :topicoId
    """)
    void desmarcarSoluciones(@org.springframework.data.repository.query.Param("topicoId") Long topicoId);

    Page<Respuesta> findByTopicoIdOrderBySolucionDescFechaCreacionDesc(Long topicoId, Pageable pageable);

    @Query("""
        select r.id, count(rh.id)
        from Respuesta r
        left join RespuestaHija rh on rh.respuesta.id = r.id
        where r.topico.id = :topicoId
        group by r.id
    """)
    List<Object[]> contarHijasPorRespuestaDeTopico(@Param("topicoId") Long topicoId);
}
