package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.curso.DatosActualizarCurso;
import com.alura.foro.hub.api.dto.curso.DatosCrearCurso;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
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

@RestController
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @Operation(
            summary = "Listar cursos",
            description = "Permite listar todos los cursos o filtrar por categoría"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cursos encontrados",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Listado de cursos",
                                    value = """
                                [
                                  {
                                    "id": 1,
                                    "nombre": "AWS",
                                    "categoriaId": 2,
                                    "categoriaNombre": "Cloud"
                                  },
                                  {
                                    "id": 2,
                                    "nombre": "Spring Boot",
                                    "categoriaId": 1,
                                    "categoriaNombre": "Backend"
                                  }
                                ]
                                """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<DatosListadoCurso>> listar(
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(cursoService.listar(categoriaId));
    }

    @Operation(
            summary = "Detallar curso",
            description = "Permite consultar un curso por su id"
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Curso encontrado",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoCurso.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Detalle de curso",
                            value = """
                        {
                          "id": 1,
                          "nombre": "AWS",
                          "categoriaId": 2,
                          "categoriaNombre": "Cloud"
                        }
                        """
                    )
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<DatosListadoCurso> detallar(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.detallar(id));
    }

    @Operation(
            summary = "Crear curso",
            description = "Permite a un administrador crear un curso nuevo",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Curso creado",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoCurso.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Curso creado",
                            value = """
                        {
                          "id": 3,
                          "nombre": "Docker",
                          "categoriaId": 2,
                          "categoriaNombre": "Cloud"
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosCrearCurso.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Crear curso",
                            value = """
                        {
                          "nombre": "Docker",
                          "categoriaId": 2
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
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
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Curso editado",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoCurso.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Curso actualizado",
                            value = """
                        {
                          "id": 1,
                          "nombre": "AWS Avanzado",
                          "categoriaId": 2,
                          "categoriaNombre": "Cloud"
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosActualizarCurso.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Actualizar curso",
                            value = """
                        {
                          "nombre": "AWS Avanzado",
                          "categoriaId": 2
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
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
    @SecurityRequirement(name = "bearer-key")
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Curso eliminado")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
