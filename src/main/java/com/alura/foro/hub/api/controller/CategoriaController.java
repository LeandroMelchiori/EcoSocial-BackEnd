//package com.alura.foro.hub.api.controller;
//
//import com.alura.foro.hub.api.domain.Categoria;
//import com.alura.foro.hub.api.service.CategoriaService;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@SecurityRequirement(name = "bearer-key")
//@RestController
//@RequestMapping("/categorias")
//public class CategoriaController {
//
//    private final CategoriaService categoriaService;
//
//    public CategoriaController(CategoriaService categoriaService) {
//        this.categoriaService = categoriaService;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Categoria>> listarCategorias() {
//        return ResponseEntity.ok(categoriaService.listarCategorias());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Categoria> obtenerCategoria(@PathVariable Long id) {
//        Optional<Categoria> categoria = categoriaService.obtenerPorId(id);
//        return categoria.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Categoria> crearCategoria(@RequestBody @Valid Categoria categoria) {
//        return ResponseEntity.ok(categoriaService.crearCategoria(categoria));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @RequestBody @Valid Categoria categoria) {
//        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, categoria));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
//        categoriaService.eliminarCategoria(id);
//        return ResponseEntity.noContent().build();
//    }
//}
