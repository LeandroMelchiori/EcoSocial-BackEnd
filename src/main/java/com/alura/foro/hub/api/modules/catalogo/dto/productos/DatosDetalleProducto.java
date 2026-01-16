package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import java.time.LocalDateTime;
import java.util.List;

public record DatosDetalleProducto(
        Long id,
        Long usuarioId,
        Long categoriaId,
        Long subcategoriaId,
        String titulo,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        List<String> imagenes
) {}

