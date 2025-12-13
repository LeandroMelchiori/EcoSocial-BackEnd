package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.domain.Curso;
import com.alura.foro.hub.api.domain.DatosListadoTopico;
import com.alura.foro.hub.api.domain.StatusTopico;
import com.alura.foro.hub.api.domain.Topico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    select new com.alura.foro.hub.api.domain.DatosListadoTopico(
        t.id,
        t.titulo,
        t.mensaje,
        t.fechaCreacion,
        a.nombre,
        c.nombre,
        t.status,
        count(r),
        max(r.fechaCreacion)
    )
    from Topico t
    join t.autor a
    join t.curso c
    left join t.respuestas r
    group by t.id, t.titulo, t.mensaje, t.fechaCreacion, a.nombre, c.nombre, t.status
    order by t.fechaCreacion desc
""")
    List<DatosListadoTopico> listarConMetricas();

}
