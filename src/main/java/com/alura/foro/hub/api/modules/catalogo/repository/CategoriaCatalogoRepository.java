package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaCatalogoRepository extends JpaRepository<CategoriaCatalogo, Long> {
}
