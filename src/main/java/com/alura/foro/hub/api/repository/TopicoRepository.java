package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopicoRepository extends
        JpaRepository<Topico, Long>,
        JpaSpecificationExecutor<Topico> {


    List<Topico> findByCurso(Curso curso);

    // Buscar tópicos por autor
    List<Topico> findByAutorUsername(String username);

    // Buscar tópicos por estado
    List<Topico> findByStatus(StatusTopico estado);

    // Buscar tópicos por título con búsqueda parcial
    List<Topico> findByTituloContainingIgnoreCase(String titulo);

    @Query("""
    select new com.alura.foro.hub.api.dto.topico.DatosListadoTopico(
        t.id,
        t.titulo,
        t.fechaCreacion,
        a.nombre,
        c.id,
        c.nombre,
        cat.id,
        cat.nombre,
        t.status,
        count(r.id),
        max(r.fechaCreacion)
    )
    from Topico t
    join t.autor a
    join t.curso c
    join c.categoria cat
    left join t.respuestas r
    where
        (:q is null or :q = '' or
            lower(t.titulo) like lower(concat('%', :q, '%')) or
            lower(t.mensaje) like lower(concat('%', :q, '%'))
        )
        and (:cursoId is null or c.id = :cursoId)
        and (:autorId is null or a.id = :autorId)
        and (:status is null or t.status = :status)
        and (:desde is null or t.fechaCreacion >= :desde)
        and (:hasta is null or t.fechaCreacion <= :hasta)
        and (:nombreCurso is null or lower(c.nombre) like lower(concat('%', :nombreCurso, '%')))
        and (:nombreCategoria is null or lower(cat.nombre) like lower(concat('%', :nombreCategoria, '%')))
    group by
        t.id, t.titulo, t.fechaCreacion, a.nombre,
        c.id, c.nombre,
        cat.id, cat.nombre,
        t.status
""")
    Page<DatosListadoTopico> buscarConMetricas(
            @Param("q") String q,
            @Param("cursoId") Long cursoId,
            @Param("autorId") Long autorId,
            @Param("status") StatusTopico status,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("nombreCurso") String nombreCurso,
            @Param("nombreCategoria") String nombreCategoria,
            Pageable pageable
    );


    @Query("""
select new com.alura.foro.hub.api.dto.topico.DatosListadoTopico(
    t.id,
    t.titulo,
    t.fechaCreacion,
    a.nombre,
    c.id,
    c.nombre,
    cat.id,
    cat.nombre,
    t.status,
    count(r),
    max(r.fechaCreacion)
)
from Topico t
join t.autor a
join t.curso c
join c.categoria cat
left join t.respuestas r
group by t.id, t.titulo, a.nombre, c.nombre, cat.nombre, t.status, t.fechaCreacion
""")
    Page<DatosListadoTopico> listarConMetricas(Pageable pageable);

    @Query("""
    select distinct t
    from Topico t
    join fetch t.autor
    join fetch t.curso c
    join fetch c.categoria
    left join fetch t.respuestas r
    left join fetch r.autor
    where t.id = :id
""")
    Optional<Topico> buscarDetallePorId(@Param("id") Long id);
}
