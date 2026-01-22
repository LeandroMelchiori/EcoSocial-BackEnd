package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import java.util.List;

public record DatosReordenarImagenes(
        List<Long> orden // lista de IDs de producto_imagenes en el orden final
) {}
