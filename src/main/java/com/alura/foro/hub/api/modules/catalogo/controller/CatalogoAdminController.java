package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.*;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.*;
import com.alura.foro.hub.api.modules.catalogo.service.CatalogoAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/catalogo/admin")
public class CatalogoAdminController {

    private final CatalogoAdminService service;

    public CatalogoAdminController(CatalogoAdminService service) {
        this.service = service;
    }

    private void exigirAdmin(Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Solo ADMIN");
        }
    }

    // -------- CATEGORIAS --------

    @PostMapping("/categorias")
    public ResponseEntity<DatosDetalleCategoriaProducto> crearCategoria(
            @RequestBody @Valid DatosCrearCategoriaProducto dto,
            Authentication auth
    ) {
        exigirAdmin(auth);
        var creado = service.crearCategoria(dto);
        return ResponseEntity.created(URI.create("/catalogo/categorias/" + creado.id())).body(creado);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarCategoriaProducto dto,
            Authentication auth
    ) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.actualizarCategoria(id, dto));
    }

    @PatchMapping("/categorias/{id}/activar")
    public ResponseEntity<Void> activarCategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.activarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarCategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.desactivarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // -------- SUBCATEGORIAS --------

    @PostMapping("/subcategorias")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> crearSubcategoria(
            @RequestBody @Valid DatosCrearSubcategoriaProducto dto,
            Authentication auth
    ) {
        exigirAdmin(auth);
        var creado = service.crearSubcategoria(dto);
        return ResponseEntity.created(URI.create("/catalogo/categorias/" + creado.categoriaId() + "/subcategorias")).body(creado);
    }

    @PutMapping("/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> actualizarSubcategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarSubcategoriaProducto dto,
            Authentication auth
    ) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.actualizarSubcategoria(id, dto));
    }

    @PatchMapping("/subcategorias/{id}/activar")
    public ResponseEntity<Void> activarSubcategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.activarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/subcategorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarSubcategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.desactivarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subcategorias/{id}")
    public ResponseEntity<Void> eliminarSubcategoria(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        service.eliminarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<DatosDetalleCategoriaProducto>> listarCategoriasAdmin(Authentication auth) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.listarCategoriasAdmin());
    }

    @GetMapping("/subcategorias")
    public ResponseEntity<List<DatosDetalleSubcategoriaProducto>> listarSubcategoriasAdmin(
            @RequestParam(required = false) Long categoriaId,
            Authentication auth
    ) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.listarSubcategoriasAdmin(categoriaId));
    }

    @GetMapping("/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> detalleSubcategoriaAdmin(
            @PathVariable Long id,
            Authentication auth
    ) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.detalleSubcategoriaAdmin(id));
    }

    @GetMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> detalleCategoriaAdmin(@PathVariable Long id, Authentication auth) {
        exigirAdmin(auth);
        return ResponseEntity.ok(service.detalleCategoriaAdmin(id));
    }

}
