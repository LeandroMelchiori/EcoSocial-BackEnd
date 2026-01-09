package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoriaCatalogoRepository extends JpaRepository<Subcategoria, Long> {
}
