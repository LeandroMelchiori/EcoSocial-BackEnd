package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.respuesta.DatosDetalleRespuesta;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.service.RespuestaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;
    public RespuestaController(
            RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @Operation(
            summary = "Crear respuesta",
            description = "Permite al usuario logueado responder a un tópico",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Respuesta creada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoRespuesta.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Respuesta creada",
                            value = """
                        {
                          "id": 45,
                          "mensaje": "Tenés que inscribirte como monotributista y emitir factura C",
                          "autorNombre": "Otro Usuario",
                          "solucion": false,
                          "fechaCreacion": "2025-12-18T19:10:00"
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosCrearRespuesta.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Crear respuesta",
                            value = """
                        {
                          "topicoId": 10,
                          "mensaje": "Tenés que inscribirte como monotributista y emitir factura C"
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
    @PostMapping
    public ResponseEntity<DatosListadoRespuesta> crear(
            @RequestBody @Valid DatosCrearRespuesta datos,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuestaService.crear(datos, usuario.getId()));
    }

    @Operation(
            summary = "Listar respuestas de un tópico",
            description = "Devuelve una lista paginada de respuestas asociadas a un tópico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de respuestas",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Página de respuestas",
                                    value = """
                                {
                                  "content": [
                                    {
                                      "id": 45,
                                      "mensaje": "Tenés que inscribirte como monotributista y emitir factura C",
                                      "autorNombre": "Otro Usuario",
                                      "solucion": false,
                                      "fechaCreacion": "2025-12-18T19:10:00"
                                    }
                                  ],
                                  "totalElements": 1,
                                  "totalPages": 1,
                                  "size": 10,
                                  "number": 0,
                                  "first": true,
                                  "last": true
                                }
                                """
                            )
                    )
            )
    })
    @GetMapping("/topico/{topicoId}")
    public ResponseEntity<Page<DatosListadoRespuesta>> listar(
            @PathVariable Long topicoId,
            Pageable pageable) {

        return ResponseEntity.ok(respuestaService.listarPorTopico(topicoId, pageable));
    }

    @Operation(
            summary = "Detalle de una respuesta",
            description = "Devuelve el detalle de una respuesta junto con sus respuestas hijas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle de la respuesta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Respuesta no encontrada"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleRespuesta> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(respuestaService.detalle(id));
    }


    @Operation(
            summary = "Marcar respuesta como solución",
            description = "Permite al autor del tópico marcar una respuesta como solución",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Respuesta marcada como solución",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoRespuesta.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Solución marcada",
                            value = """
                        {
                          "id": 45,
                          "mensaje": "Tenés que inscribirte como monotributista y emitir factura C",
                          "autorNombre": "Otro Usuario",
                          "solucion": true,
                          "fechaCreacion": "2025-12-18T19:10:00"
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
    @PatchMapping("/{id}/solucion")
    public ResponseEntity<DatosListadoRespuesta> marcarSolucion(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                respuestaService.marcarSolucion(id, usuario.getId())
        );
    }

    @Operation(
            summary = "Actualizar respuesta",
            description = "Permite al autor de la respuesta modificar su contenido",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Respuesta actualizada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoRespuesta.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Respuesta actualizada",
                            value = """
                        {
                          "id": 45,
                          "mensaje": "Actualicé la respuesta con más detalle",
                          "autorNombre": "Otro Usuario",
                          "solucion": false,
                          "fechaCreacion": "2025-12-18T19:10:00"
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosActualizarRespuesta.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Actualizar respuesta",
                            value = """
                        {
                          "mensaje": "Actualicé la respuesta con más detalle"
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoRespuesta> editar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarRespuesta datos,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                respuestaService.actualizar(id, datos, usuario.getId())
        );
    }


    @Operation(
            summary = "Eliminar respuesta",
            description = "Permite al autor de la respuesta" +
                    " (tambien autor del topico o admin) eliminar la respuesta seleccionada",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @SecurityRequirement(name = "bearer-key")
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Respuesta eliminada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        respuestaService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
