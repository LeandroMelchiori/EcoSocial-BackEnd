package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Listar cursos",
            description = "Permite listar todos los cursos")
    @GetMapping
    public ResponseEntity<List<DatosListadoCurso>> listar(
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(cursoService.listar(categoriaId));
    }

    @Operation(
            summary = "Detallar curso",
            description = "Permite consultar un curso por su id")
    @ApiResponses({@ApiResponse(responseCode = "404", description = "Curso no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<DatosListadoCurso> detallar(
            @PathVariable Long id) {
        return ResponseEntity.ok(cursoService.detallar(id));
    }

    @Operation(
            summary = "Crear curso",
            description = "Permite a un administrador crear un curso nuevo",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curso creado con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina inexistente"),
            @ApiResponse(responseCode = "409", description = "Ya existe un curso con ese nombre")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCurso> crear(
            @RequestBody @Valid DatosCrearCurso datos) {
        var creado = cursoService.crear(datos);
        return ResponseEntity.created(URI.create("/cursos/" + creado.id())).body(creado);
    }

    @Operation(
            summary = "Editar curso",
            description = "Permite a un administrador editar un curso",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curso editado con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un curso con ese nombre")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCurso> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarCurso datos) {
        return ResponseEntity.ok(cursoService.actualizar(id, datos));
    }

    @Operation(
            summary = "Eliminar curso",
            description = "Permite a un administrador eliminar un curso",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curso eliminado con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar. " +
                    "Existen recursos adheridos a este curso")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
