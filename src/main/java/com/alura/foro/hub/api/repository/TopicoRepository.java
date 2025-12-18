package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.entity.model.Curso;
import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicoRepository extends JpaRepository<Topico, Long> {

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
