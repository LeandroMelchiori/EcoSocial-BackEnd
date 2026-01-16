package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoriaCatalogoRepository extends JpaRepository<Subcategoria, Long> {
    List<Subcategoria> findByCategoria_IdAndActivoTrueOrderByNombreAsc(Long categoriaId);
    boolean existsByCategoria_Id(Long categoriaId);
    boolean existsByCategoria_IdAndNombreIgnoreCase(Long categoriaId, String nombre);
    Optional<Subcategoria> findByCategoria_IdAndNombreIgnoreCase(Long categoriaId, String nombre);

    List<Subcategoria> findAllByOrderByNombreAsc();
    List<Subcategoria> findByCategoria_IdOrderByNombreAsc(Long categoriaId);

}
