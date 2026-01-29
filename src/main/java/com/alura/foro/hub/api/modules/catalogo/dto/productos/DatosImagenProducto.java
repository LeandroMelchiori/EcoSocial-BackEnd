package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Imagen asociada a un producto")
public record DatosImagenProducto(

        @Schema(example = "100")
        Long id,

        @Schema(
                description = "Orden de la imagen",
                example = "0"
        )
        Integer orden,

        @Schema(
                description = "URL pública de la imagen",
                example = "https://cdn.midominio.com/productos/pan-1.jpg"
        )
        String url
) {}
