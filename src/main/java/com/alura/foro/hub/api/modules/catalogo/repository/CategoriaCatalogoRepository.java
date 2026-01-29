package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaCatalogoRepository extends JpaRepository<CategoriaCatalogo, Long> {
    List<CategoriaCatalogo> findByActivoTrueOrderByNombreAsc();

    Optional<CategoriaCatalogo> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

    List<CategoriaCatalogo> findAllByOrderByNombreAsc();

    boolean existsByIdAndActivoTrue(Long id);

}
