package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.service.RespuestaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
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
            description = "Permite al usuario logueado hacer un comentario en el topico seleccionado",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Respuesta creada con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Error al cargar la pagina solicitada")
    })
    @PostMapping
    public ResponseEntity<DatosListadoRespuesta> crear(
            @RequestBody @Valid DatosCrearRespuesta datos,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(respuestaService.crear(datos, usuario.getId())
        );
    }

    @Operation(summary = "Listar respuestas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Respuestas encontradas")})
    @GetMapping("/topico/{topicoId}")
    public ResponseEntity<Page<DatosListadoRespuesta>> listar(
            @PathVariable Long topicoId,
            Pageable pageable) {
        return ResponseEntity.ok(respuestaService.listarPorTopico(topicoId, pageable));
    }

    @Operation(
            summary = "Marcar respuesta como solucion",
            description = "Permite al autor del tópico marcar una respuesta como la solucion al topico",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solucion marcada con exito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Respuesta no encontrada")
    })
    @PatchMapping("/{id}/solucion")
    public ResponseEntity<DatosListadoRespuesta> marcarSolucion(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                respuestaService.marcarSolucion(id, usuario.getId())
        );
    }

    @Operation(
            summary = "Actualizar respuesta",
            description = "Permite al autor del tópico modificar su contenido o cambiar su curso.",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Respuesta actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoRespuesta> editar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarRespuesta datos,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(respuestaService.actualizar(id, datos, usuario.getId())
        );
    }

    @Operation(
            summary = "Eliminar respuesta",
            description = "Permite al autor de la respuesta" +
                    " (tambien autor del topico o admin) eliminar la respuesta seleccionada",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Respuesta eliminada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "No se encuentra la pagina solicitada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        respuestaService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
