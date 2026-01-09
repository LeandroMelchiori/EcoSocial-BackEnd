package com.alura.foro.hub.api.modules.catalogo.repository;

import com.alura.foro.hub.api.modules.catalogo.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
