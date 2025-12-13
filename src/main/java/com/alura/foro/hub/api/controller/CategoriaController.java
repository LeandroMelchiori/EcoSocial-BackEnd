package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.dto.categoria.*;
import com.alura.foro.hub.api.domain.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.service.CategoriaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<DatosListadoCategoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}/cursos")
    public ResponseEntity<List<DatosListadoCurso>> listarCursos(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.listarCursosDeCategoria(id));
    }

    @PostMapping
    public ResponseEntity<DatosListadoCategoria> crear(@RequestBody @Valid DatosCrearCategoria datos) {
        var creada = categoriaService.crear(datos);
        return ResponseEntity.created(URI.create("/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoCategoria> actualizar(@PathVariable Long id,
                                                            @RequestBody @Valid DatosActualizarCategoria datos) {
        return ResponseEntity.ok(categoriaService.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
