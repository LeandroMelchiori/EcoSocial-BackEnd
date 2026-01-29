package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosDetalleCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosDetalleSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.service.CategoriaCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catálogo Público", description = "Endpoints públicos para consumir categorías y subcategorías activas.")
@RestController
@RequestMapping("/catalogo")
public class CategoriaCatalogoController {

    private final CategoriaCatalogoService service;

    public CategoriaCatalogoController(CategoriaCatalogoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar categorías activas",
            description = "Devuelve únicamente categorías activas para que el front muestre opciones al crear productos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de categorías activas",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                [
                  { "id": 1, "nombre": "Alimentos", "activo": true },
                  { "id": 2, "nombre": "Indumentaria", "activo": true }
                ]
                """)
            )
    )
    @GetMapping("/categorias")
    public ResponseEntity<List<DatosDetalleCategoriaProducto>> categorias() {
        return ResponseEntity.ok(service.listarCategoriasActivas());
    }

    @Operation(
            summary = "Listar subcategorías activas de una categoría",
            description = "Devuelve las subcategorías activas de la categoría indicada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de subcategorías activas",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                [
                  { "id": 10, "categoriaId": 1, "nombre": "Panificados", "activo": true },
                  { "id": 11, "categoriaId": 1, "nombre": "Conservas", "activo": true }
                ]
                """)
            )
    )
    @GetMapping("/categorias/{categoriaId}/subcategorias")
    public ResponseEntity<List<DatosDetalleSubcategoriaProducto>> subcategorias(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarSubcategoriasActivas(categoriaId));
    }
}
