package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.service.CategoriaService;
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

    @Operation(
            summary = "Listar cursos de una categoria",
            description = "Permite listar todos los cursos de una categoria")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cursos encontrados")})
    @GetMapping("/{id}/cursos")
    public ResponseEntity<List<DatosListadoCurso>> listarCursos(
            @PathVariable Long id) {

        return ResponseEntity.ok(categoriaService.listarCursosDeCategoria(id));
    }

    @Operation(
            summary = "Crear categoria",
            description = "Permite a un administrador crear una categoria nueva",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria creada con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina inexistente"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoria con ese nombre")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCategoria> crear(
            @RequestBody @Valid DatosCrearCategoria datos) {

        var creada = categoriaService.crear(datos);
        return ResponseEntity.created(URI.create("/categorias/" + creada.id())).body(creada);
    }

    @Operation(
            summary = "Actualizar categoria",
            description = "Permite a un administrador editar una categoria existente",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria actualizada con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoria con ese nombre")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatosListadoCategoria> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarCategoria datos) {

        return ResponseEntity.ok(categoriaService.actualizar(id, datos));
    }

    @Operation(
            summary = "Eliminar categoria",
            description = "Permite a un administrador eliminar una categoria existente",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria eliminada con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina inexistente"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar. existen cursos" +
                    "adheridos a esta categoria"),
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
