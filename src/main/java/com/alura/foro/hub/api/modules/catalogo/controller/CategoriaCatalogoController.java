package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosDetalleCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosDetalleSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.service.CategoriaCatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogo")
public class CategoriaCatalogoController {

    private final CategoriaCatalogoService service;

    public CategoriaCatalogoController(CategoriaCatalogoService service) {
        this.service = service;
    }

    // GET /catalogo/categorias
    @GetMapping("/categorias")
    public ResponseEntity<List<DatosDetalleCategoriaProducto>> categorias() {
        return ResponseEntity.ok(service.listarCategoriasActivas());
    }

    // GET /catalogo/categorias/{id}/subcategorias
    @GetMapping("/categorias/{categoriaId}/subcategorias")
    public ResponseEntity<List<DatosDetalleSubcategoriaProducto>> subcategorias(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarSubcategoriasActivas(categoriaId));
    }
}
