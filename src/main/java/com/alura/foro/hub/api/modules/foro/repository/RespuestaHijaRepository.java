package com.alura.foro.hub.api.modules.foro.repository;

import com.alura.foro.hub.api.modules.foro.domain.model.RespuestaHija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RespuestaHijaRepository extends JpaRepository<RespuestaHija, Long> {

    @Query("""
        select rh
        from RespuestaHija rh
        join fetch rh.autor
        where rh.respuesta.id = :respuestaId
        order by rh.fechaCreacion desc
    """)
    List<RespuestaHija> buscarPorRespuestaConAutor(@Param("respuestaId") Long respuestaId);
}
