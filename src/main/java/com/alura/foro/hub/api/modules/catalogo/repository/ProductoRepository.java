package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.Producto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosListadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
    SELECT new com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosListadoProducto(
        p.id,
        p.titulo,
        p.activo,
        p.fechaCreacion,
        p.usuario.id,
        p.categoria.id,
        CASE WHEN p.subcategoria IS NULL THEN NULL ELSE p.subcategoria.id END,
        (
            SELECT pi.url
            FROM ProductoImagen pi
            WHERE pi.producto = p AND pi.orden = 1
        )
    )
    FROM Producto p
    WHERE p.activo = true
      AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
      AND (:subcategoriaId IS NULL OR p.subcategoria.id = :subcategoriaId)
      AND (:q IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<DatosListadoProducto> buscar(@Param("categoriaId") Long categoriaId,
                                             @Param("subcategoriaId") Long subcategoriaId,
                                             @Param("q") String q,
                                             Pageable pageable);


    @EntityGraph(attributePaths = {"imagenes", "usuario", "categoria", "subcategoria"})
    Optional<Producto> findWithImagenesById(Long id);

    boolean existsByCategoria_Id(Long categoriaId);
    boolean existsBySubcategoria_Id(Long subcategoriaId);

}