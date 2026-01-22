package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.*;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.*;
import com.alura.foro.hub.api.modules.catalogo.service.CatalogoAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/catalogo/admin")
@PreAuthorize("hasRole('ADMIN')") // <- aplica a TODOS los endpoints del controller
public class CatalogoAdminController {

    private final CatalogoAdminService service;

    public CatalogoAdminController(CatalogoAdminService service) {
        this.service = service;
    }

    // -------- CATEGORIAS --------

    @PostMapping("/categorias")
    public ResponseEntity<DatosDetalleCategoriaProducto> crearCategoria(
            @RequestBody @Valid DatosCrearCategoriaProducto dto
    ) {
        var creado = service.crearCategoria(dto);
        return ResponseEntity.created(URI.create("/catalogo/categorias/" + creado.id())).body(creado);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarCategoriaProducto dto
    ) {
        return ResponseEntity.ok(service.actualizarCategoria(id, dto));
    }

    @PatchMapping("/categorias/{id}/activar")
    public ResponseEntity<Void> activarCategoria(@PathVariable Long id) {
        service.activarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarCategoria(@PathVariable Long id) {
        service.desactivarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        service.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // -------- SUBCATEGORIAS --------

    @PostMapping("/subcategorias")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> crearSubcategoria(
            @RequestBody @Valid DatosCrearSubcategoriaProducto dto
    ) {
        var creado = service.crearSubcategoria(dto);
        return ResponseEntity.created(
                URI.create("/catalogo/categorias/" + creado.categoriaId() + "/subcategorias")
        ).body(creado);
    }

    @PutMapping("/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> actualizarSubcategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarSubcategoriaProducto dto
    ) {
        return ResponseEntity.ok(service.actualizarSubcategoria(id, dto));
    }

    @PatchMapping("/subcategorias/{id}/activar")
    public ResponseEntity<Void> activarSubcategoria(@PathVariable Long id) {
        service.activarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/subcategorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarSubcategoria(@PathVariable Long id) {
        service.desactivarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subcategorias/{id}")
    public ResponseEntity<Void> eliminarSubcategoria(@PathVariable Long id) {
        service.eliminarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    // -------- LISTADOS / DETALLES ADMIN --------

    @GetMapping("/categorias")
    public ResponseEntity<List<DatosDetalleCategoriaProducto>> listarCategoriasAdmin() {
        return ResponseEntity.ok(service.listarCategoriasAdmin());
    }

    @GetMapping("/subcategorias")
    public ResponseEntity<List<DatosDetalleSubcategoriaProducto>> listarSubcategoriasAdmin(
            @RequestParam(required = false) Long categoriaId
    ) {
        return ResponseEntity.ok(service.listarSubcategoriasAdmin(categoriaId));
    }

    @GetMapping("/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> detalleSubcategoriaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalleSubcategoriaAdmin(id));
    }

    @GetMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> detalleCategoriaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalleCategoriaAdmin(id));
    }
}
