package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.domain.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.domain.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.service.CursoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // GET /cursos o GET /cursos?categoriaId=1
    @GetMapping
    public ResponseEntity<List<DatosListadoCurso>> listar(@RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(cursoService.listar(categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosListadoCurso> detallar(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.detallar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCurso> crear(@RequestBody @Valid DatosCrearCurso datos) {
        var creado = cursoService.crear(datos);
        return ResponseEntity.created(URI.create("/cursos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCurso> actualizar(@PathVariable Long id,
                                                        @RequestBody @Valid DatosActualizarCurso datos) {
        return ResponseEntity.ok(cursoService.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
