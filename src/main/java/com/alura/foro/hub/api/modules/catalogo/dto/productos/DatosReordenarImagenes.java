package com.alura.foro.hub.api.modules.catalogo.dto.productos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Nuevo orden de las imágenes del producto")
public record DatosReordenarImagenes(

        @Schema(
                description = "Lista de IDs de imágenes en el orden final",
                example = "[101, 100, 102]"
        )
        List<Long> orden
) {}
