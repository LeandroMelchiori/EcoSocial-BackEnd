package com.alura.foro.hub.api.modules.foro.controller;

import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosActualizarRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosCrearRespuestaHija;
import com.alura.foro.hub.api.modules.foro.dto.respuestaHija.DatosListadoRespuestaHija;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.modules.foro.service.RespuestaHijaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/respuestas")
public class RespuestaHijaController {

    private final RespuestaHijaService respuestaHijaService;

    public RespuestaHijaController(RespuestaHijaService respuestaHijaService) {
        this.respuestaHijaService = respuestaHijaService;
    }

    @Operation(
            summary = "Crear respuesta hija",
            description = "Permite al usuario logueado responder a una respuesta (nivel 2, tipo Instagram)",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Respuesta hija creada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoRespuestaHija.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Respuesta hija creada",
                            value = """
                        {
                          "id": 10,
                          "mensaje": "Estoy de acuerdo, sumo un detalle...",
                          "autorNombre": "Otro Usuario",
                          "fechaCreacion": "2025-12-18T19:10:00",
                          "editado": false
                        }
                        """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosCrearRespuestaHija.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Crear respuesta hija",
                            value = """
                        {
                          "mensaje": "Estoy de acuerdo, sumo un detalle..."
                        }
                        """
                    )
            )
    )
    @SecurityRequirement(name = "bearer-key")
    @PostMapping("/{respuestaId}/hijas")
    public ResponseEntity<DatosListadoRespuestaHija> crear(
            @PathVariable Long respuestaId,
            @RequestBody @Valid DatosCrearRespuestaHija datos,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuestaHijaService.crear(respuestaId, datos, usuario.getId()));
    }

    @Operation(
            summary = "Listar respuestas hijas de una respuesta",
            description = "Devuelve la lista de respuestas hijas asociadas a una respuesta (se cargan on-demand)"
    )

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de respuestas hijas",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Lista de respuestas hijas",
                                    value = """
                                [
                                  {
                                    "id": 10,
                                    "mensaje": "Estoy de acuerdo, sumo un detalle...",
                                    "autorNombre": "Otro Usuario",
                                    "fechaCreacion": "2025-12-18T19:10:00",
                                    "editado": false
                                  }
                                ]
                                """
                            )
                    )
            )
    })
    @GetMapping("/{respuestaId}/hijas")
    public ResponseEntity<List<DatosListadoRespuestaHija>> listar(@PathVariable Long respuestaId) {
        return ResponseEntity.ok(respuestaHijaService.listarPorRespuesta(respuestaId));
    }

    @Operation(
            summary = "Eliminar respuesta hija",
            description = "Permite al autor de la respuesta" +
                    " (tambien autor del topico o admin) eliminar la respuesta hija seleccionada"
    )
    @SecurityRequirement(name = "bearer-key")
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Respuesta eliminada")
    @DeleteMapping("/hijas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        respuestaHijaService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Actualizar respuesta hija",
            description = "Permite al autor de la respuesta hija modificar su contenido",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Respuesta hija actualizada",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosListadoRespuestaHija.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Respuesta hija actualizada",
                            value = """
                                {
                                  "id": 10,
                                  "mensaje": "Actualicé la respuesta hija con más detalle",
                                  "autorNombre": "Otro Usuario",
                                  "fechaCreacion": "2025-12-18T19:10:00",
                                  "editado": false
                                }
                                """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatosActualizarRespuestaHija.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "Actualizar respuesta hija",
                            value = """
                                {
                                  "mensaje": "Actualicé la respuesta hija con más detalle"
                                }
                                """
                    )
            )
    )
    @PutMapping("/hijas/{id}")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<DatosListadoRespuestaHija> editar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarRespuestaHija datos,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                respuestaHijaService.actualizar(id, datos, usuario.getId())
        );
    }
}
