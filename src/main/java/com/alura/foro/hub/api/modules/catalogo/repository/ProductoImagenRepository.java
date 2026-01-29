package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    @Modifying
    @Query("delete from ProductoImagen pi where pi.producto.id = :productoId")
    void deleteAllByProductoId(@Param("productoId") Long productoId);
}
