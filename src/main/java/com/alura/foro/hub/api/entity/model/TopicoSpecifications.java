package com.alura.foro.hub.api.entity.model;

import com.alura.foro.hub.api.dto.topico.TopicoFiltro;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class TopicoSpecifications {

    public static Specification<Topico> conFiltro(TopicoFiltro f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // q: busca en título o mensaje (ajustá nombres de campos)
            if (f.q() != null && !f.q().isBlank()) {
                var like = "%" + f.q().trim().toLowerCase() + "%";
                var titulo = cb.like(cb.lower(root.get("titulo")), like);
                var mensaje = cb.like(cb.lower(root.get("mensaje")), like);
                predicates.add(cb.or(titulo, mensaje));
            }

            // cursoId (si Topico tiene curso con relación ManyToOne)
            if (f.cursoId() != null) {
                predicates.add(cb.equal(root.get("curso").get("id"), f.cursoId()));
            }

            // autorId (si Topico tiene autor/usuario)
            if (f.autorId() != null) {
                predicates.add(cb.equal(root.get("autor").get("id"), f.autorId()));
            }

            // status
            if (f.status() != null) {
                predicates.add(cb.equal(root.get("status"), f.status()));
            }

            // rango fechas
            if (f.desde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), f.desde()));
            }
            if (f.hasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), f.hasta()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
