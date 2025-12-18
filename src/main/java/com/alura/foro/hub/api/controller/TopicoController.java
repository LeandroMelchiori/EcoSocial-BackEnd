package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.service.TopicoService;
import io.swagger.v3.oas.annotations.Operation;
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

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicoService topicoService;

    public TopicoController(TopicoService topicoService) {
        this.topicoService = topicoService;
    }

    // LISTAR TODOS
    @Operation(
            summary = "Listar todos los topicos",
            description = "Permite listar todos los topicos creados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Topicos encontrados")})
    @GetMapping
    public ResponseEntity<Page<DatosListadoTopico>> listar(Pageable pageable) {
        return ResponseEntity.ok(topicoService.listar(pageable));
    }

    // CREAR
    @Operation(
            summary = "Crear tópico",
            description = "Permite al usuario logueado crear un topico",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópico actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina no encontrada")
    })
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
    @Operation(
            summary = "Actualizar tópico",
            description = "Permite al autor del tópico modificar su contenido o cambiar su curso.",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópico actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina no encontrada")
    })
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
    @Operation(
            summary = "Borrar un topico",
            description = "Permite al autor (o admin) borrar el topico seleccionado",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tópico eliminado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pagina no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        topicoService.eliminarTopico(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    // DETALLAR POR ID
    @Operation(
            summary = "Detallar un topico",
            description = "Despliega detalle completo del topico con sus respuestas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Topico detallado con exito")})
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> detallar(@PathVariable Long id) {
        DatosDetalleTopico dto = topicoService.detallarTopico(id);
        return ResponseEntity.ok(dto);
    }
}
