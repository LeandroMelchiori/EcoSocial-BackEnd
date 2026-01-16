package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import java.time.LocalDateTime;

public record DatosListadoProducto(
        Long id,
        String titulo,
        Boolean activo,
        LocalDateTime fechaCreacion,
        Long usuarioId,
        Long categoriaId,
        Long subcategoriaId,
        String thumbnailUrl
) {}
