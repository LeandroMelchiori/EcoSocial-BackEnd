package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.service.TopicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicoService topicoService;

    public TopicoController(TopicoService topicoService) {
        this.topicoService = topicoService;
    }

    // LISTAR TODOS
    @Operation(
            summary = "Listar todos los tópicos",
            description = "Devuelve una lista paginada de tópicos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado paginado de tópicos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Página de tópicos",
                                    value = """
                                {
                                  "content": [
                                    {
                                      "id": 10,
                                      "titulo": "Consulta sobre monotributo",
                                      "fechaCreacion": "2025-12-18T18:30:00",
                                      "nombreAutor": "Leandro",
                                      "cursoId": 1,
                                      "nombreCurso": "Emprendimientos digitales",
                                      "categoriaId": 2,
                                      "nombreCategoria": "Economía social",
                                      "status": "ABIERTO",
                                      "cantidadRespuestas": 1,
                                      "fechaUltimaRespuesta": "2025-12-18T19:10:00"
                                    }
                                  ],
                                  "pageable": {
                                    "pageNumber": 0,
                                    "pageSize": 10,
                                    "sort": {
                                      "sorted": true,
                                      "unsorted": false,
                                      "empty": false
                                    },
                                    "offset": 0,
                                    "paged": true,
                                    "unpaged": false
                                  },
                                  "totalPages": 1,
                                  "totalElements": 1,
                                  "last": true,
                                  "size": 10,
                                  "number": 0,
                                  "sort": {
                                    "sorted": true,
                                    "unsorted": false,
                                    "empty": false
                                  },
                                  "numberOfElements": 1,
                                  "first": true,
                                  "empty": false
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<DatosListadoTopico>> listar(Pageable pageable) {
        return ResponseEntity.ok(topicoService.listar(pageable));
    }

    // CREAR
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Crear tópico",
            description = "Permite al usuario logueado crear un topico",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Tópico creado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleTopico.class),
                    examples = @ExampleObject(
                            name = "Creación exitosa",
                            value = """
                        {
                          "id": 10,
                          "titulo": "Consulta sobre monotributo",
                          "mensaje": "¿Cómo facturo si vendo por Instagram?",
                          "fechaCreacion": "2025-12-18T18:30:00",
                          "autorNombre": "Leandro",
                          "cursoId": 1,
                          "cursoNombre": "Emprendimientos digitales",
                          "categoriaId": 2,
                          "categoriaNombre": "Economía social",
                          "status": "ABIERTO",
                          "respuestas": []
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosRegistroTopico.class),
                    examples = @ExampleObject(
                            name = "Crear tópico",
                            value = """
                        {
                          "titulo": "Consulta sobre monotributo",
                          "mensaje": "¿Cómo facturo si vendo por Instagram?",
                          "cursoId": 1
                        }
                        """
                    )
            )
    )
    @PostMapping
    public ResponseEntity<DatosDetalleTopico> crear(
            @RequestBody @Valid DatosRegistroTopico datos,
            @AuthenticationPrincipal Usuario usuario) {

        var dto = topicoService.crearTopico(datos, usuario.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dto);
    }

    // ✏️ ACTUALIZAR POR ID
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Actualizar tópico",
            description = "Permite al autor del tópico modificar su contenido o cambiar su curso.",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Tópico actualizado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleTopico.class),
                    examples = @ExampleObject(
                            name = "Actualización exitosa",
                            value = """
                        {
                          "id": 10,
                          "titulo": "Consulta sobre monotributo (actualizada)",
                          "mensaje": "Ya me inscribí. ¿Cómo emito la primera factura?",
                          "fechaCreacion": "2025-12-18T18:30:00",
                          "autorNombre": "Leandro",
                          "cursoId": 1,
                          "cursoNombre": "Emprendimientos digitales",
                          "categoriaId": 2,
                          "categoriaNombre": "Economía social",
                          "status": "ABIERTO",
                          "respuestas": []
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosActualizarTopico.class),
                    examples = @ExampleObject(
                            name = "Actualizar tópico",
                            value = """
                        {
                          "titulo": "Consulta sobre monotributo (actualizada)",
                          "mensaje": "Ya me inscribí. ¿Cómo emito la primera factura?",
                          "cursoId": 1,
                          "status": "ABIERTO"
                        }
                        """
                    )
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarTopico datos,
            @AuthenticationPrincipal Usuario usuario
    ) {

        var dto = topicoService.actualizarTopico(id, datos, usuario.getId());
        return ResponseEntity.ok(dto);
    }

    // 🗑️ ELIMINAR POR ID
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Borrar un topico",
            description = "Permite al autor (o admin) borrar el topico seleccionado",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Topico eliminado con exito")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        topicoService.eliminarTopico(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    // DETALLAR POR ID
    @Operation(
            summary = "Detallar un tópico",
            description = "Devuelve el detalle completo del tópico, incluyendo respuestas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tópico detallado con éxito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DatosDetalleTopico.class),
                            examples = @ExampleObject(
                                    name = "Detalle de tópico",
                                    value = """
                                {
                                  "id": 10,
                                  "titulo": "Consulta sobre monotributo",
                                  "mensaje": "¿Cómo facturo si vendo por Instagram?",
                                  "fechaCreacion": "2025-12-18T18:30:00",
                                  "autorNombre": "Leandro",
                                  "cursoId": 1,
                                  "cursoNombre": "Emprendimientos digitales",
                                  "categoriaId": 2,
                                  "categoriaNombre": "Economía social",
                                  "status": "ABIERTO",
                                  "respuestas": [
                                    {
                                      "id": 45,
                                      "mensaje": "Tenés que inscribirte como monotributista y emitir factura C",
                                      "fechaCreacion": "2025-12-18T19:10:00",
                                      "autorNombre": "Otro Usuario"
                                    }
                                  ]
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> detallar(@PathVariable Long id) {
        DatosDetalleTopico dto = topicoService.detallarTopico(id);
        return ResponseEntity.ok(dto);
    }
}
