package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
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

    @Operation(
            summary = "Listar categorias",
            description = "Permite listar todas las categorias"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias encontradas",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Listado de categorias",
                                    value = """
                                [
                                  { "id": 1, "nombre": "Backend" },
                                  { "id": 2, "nombre": "Cloud" }
                                ]
                                """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<DatosListadoCategoria>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @Operation(
            summary = "Listar cursos de una categoria",
            description = "Permite listar todos los cursos de una categoria"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cursos encontrados",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Cursos por categoria",
                                    value = """
                                [
                                  {
                                    "id": 1,
                                    "nombre": "AWS",
                                    "categoriaId": 2,
                                    "categoriaNombre": "Cloud"
                                  }
                                ]
                                """
                            )
                    )
            )
    })
    @GetMapping("/{id}/cursos")
    public ResponseEntity<List<DatosListadoCurso>> listarCursos(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.listarCursosDeCategoria(id));
    }

    @Operation(
            summary = "Crear categoria",
            description = "Permite a un administrador crear una categoria nueva",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Categoria creada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoCategoria.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Categoria creada",
                            value = """
                        {
                          "id": 3,
                          "nombre": "DevOps"
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosCrearCategoria.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Crear categoria",
                            value = """
                        {
                          "nombre": "DevOps"
                        }
                        """
                    )
            )
    )
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
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Categoria editada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoCategoria.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "<Categoria> actualizada",
                            value = """
                        {
                          "id": 1,
                          "nombre": "AWS",
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosActualizarCategoria.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Actualizar categoria",
                            value = """
                        {
                          "nombre": "AWS",
                        }
                        """
                    )
            )
    )
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
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Categoria eliminada")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}