package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.*;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.*;
import com.alura.foro.hub.api.modules.catalogo.service.CatalogoAdminService;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "CRUD Categorias/subcategorias Admin", description = "Endpoints de administración de categorías y subcategorías. Requiere rol ADMIN.")
@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/catalogo/admin")
@PreAuthorize("hasRole('ADMIN')")
public class CatalogoAdminController {

    private final CatalogoAdminService service;

    public CatalogoAdminController(CatalogoAdminService service) {
        this.service = service;
    }

    // -------- CATEGORIAS --------

    @Operation(
            summary = "Crear categoría",
            description = "Crea una nueva categoría del catálogo (por defecto puede crearse activa o inactiva según tu lógica de servicio)."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Categoría creada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleCategoriaProducto.class),
                    examples = @ExampleObject(value = """
                {
                  "id": 1,
                  "nombre": "Alimentos",
                  "activo": true
                }
                """)
            )
    )
    @PostMapping("/categorias")
    public ResponseEntity<DatosDetalleCategoriaProducto> crearCategoria(
            @RequestBody @Valid DatosCrearCategoriaProducto dto
    ) {
        var creado = service.crearCategoria(dto);
        return ResponseEntity.created(URI.create("/catalogo/categorias/" + creado.id())).body(creado);
    }

    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza el nombre de una categoría (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Categoría actualizada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleCategoriaProducto.class),
                    examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "nombre": "Alimentos y bebidas",
                      "activo": true
                    }
                    """)
            )
    )
    @PutMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarCategoriaProducto dto
    ) {
        return ResponseEntity.ok(service.actualizarCategoria(id, dto));
    }


    @Operation(
            summary = "Activar categoría",
            description = "Marca una categoría como activa. Las categorías activas son las que se muestran en el catálogo público."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Categoría activada")
    @PatchMapping("/categorias/{id}/activar")
    public ResponseEntity<Void> activarCategoria(@PathVariable Long id) {
        service.activarCategoria(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Desactivar categoría",
            description = "Marca una categoría como inactiva. Deja de aparecer en el catálogo público."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Categoría desactivada")
    @PatchMapping("/categorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarCategoria(@PathVariable Long id) {
        service.desactivarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Eliminar categoría",
            description = """
                    Elimina una categoría.\s
                    Recomendación de negocio: si tiene productos asociados, devolvé 409 (Conflict).
                   \s"""
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        service.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // -------- SUBCATEGORIAS --------

    @Operation(
            summary = "Crear subcategoría",
            description = "Crea una subcategoría para una categoría existente (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Subcategoría creada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleSubcategoriaProducto.class),
                    examples = @ExampleObject(value = """
                    {
                      "id": 10,
                      "categoriaId": 1,
                      "nombre": "Panificados",
                      "activo": true
                    }
                    """)
            )
    )
    @PostMapping("/categorias/{categoriaId}/subcategorias")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> crearSubcategoria(
            @RequestBody @Valid DatosCrearSubcategoriaProducto dto
    ) {
        var creado = service.crearSubcategoria(dto);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @Operation(
            summary = "Actualizar subcategoría",
            description = "Actualiza el nombre y/o categoría asociada de una subcategoría (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Subcategoría actualizada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleSubcategoriaProducto.class),
                    examples = @ExampleObject(value = """
                    {
                      "id": 10,
                      "categoriaId": 1,
                      "nombre": "Panificados artesanales",
                      "activo": true
                    }
                    """)
            )
    )
    @PutMapping("/categorias/{categoriaId}/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> actualizarSubcategoria(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarSubcategoriaProducto dto
    ) {
        return ResponseEntity.ok(service.actualizarSubcategoria(id, dto));
    }

    @Operation(
            summary = "Activar subcategoría",
            description = "Marca una subcategoría como activa (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Subcategoría activada")
    @PatchMapping("/subcategorias/{id}/activar")
    public ResponseEntity<Void> activarSubcategoria(@PathVariable Long id) {
        service.activarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Desactivar subcategoría",
            description = "Marca una subcategoría como inactiva (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Subcategoría desactivada")
    @PatchMapping("/subcategorias/{id}/desactivar")
    public ResponseEntity<Void> desactivarSubcategoria(@PathVariable Long id) {
        service.desactivarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Eliminar subcategoría",
            description = """
                    Elimina una subcategoría.
                    Recomendación de negocio: si tiene productos asociados, devolvé 409 (Conflict).
                    """
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Subcategoría eliminada")
    @DeleteMapping("/subcategorias/{id}")
    public ResponseEntity<Void> eliminarSubcategoria(@PathVariable Long id) {
        service.eliminarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }

    // -------- LISTADOS / DETALLES ADMIN --------

    @Operation(
            summary = "Listar categorías (admin)",
            description = "Lista todas las categorías, incluyendo inactivas (solo ADMIN)."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Listado de categorías",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                    [
                      { "id": 1, "nombre": "Alimentos", "activo": true },
                      { "id": 2, "nombre": "Indumentaria", "activo": false }
                    ]
                    """)
            )
    )
    @GetMapping("/categorias")
    public ResponseEntity<List<DatosDetalleCategoriaProducto>> listarCategoriasAdmin() {
        return ResponseEntity.ok(service.listarCategoriasAdmin());
    }

    @Operation(
            summary = "Listar subcategorías (admin)",
            description = """
                    Lista subcategorías, incluyendo inactivas (solo ADMIN).
                    Si se envía categoriaId, filtra por esa categoría.
                    """
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Listado de subcategorías",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                    [
                      { "id": 10, "categoriaId": 1, "nombre": "Panificados", "activo": true },
                      { "id": 11, "categoriaId": 1, "nombre": "Conservas", "activo": false }
                    ]
                    """)
            )
    )
    @GetMapping("/subcategorias")
    public ResponseEntity<List<DatosDetalleSubcategoriaProducto>> listarSubcategoriasAdmin(
            @RequestParam(required = false) Long categoriaId
    ) {
        return ResponseEntity.ok(service.listarSubcategoriasAdmin(categoriaId));
    }

    @Operation(
            summary = "Detalle subcategoría (admin)",
            description = "Devuelve el detalle de una subcategoría (incluye si está activa o no). Solo ADMIN."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Detalle de subcategoría",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleSubcategoriaProducto.class),
                    examples = @ExampleObject(value = """
                    { "id": 10, "categoriaId": 1, "nombre": "Panificados", "activo": true }
                    """)
            )
    )
    @GetMapping("/subcategorias/{id}")
    public ResponseEntity<DatosDetalleSubcategoriaProducto> detalleSubcategoriaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalleSubcategoriaAdmin(id));
    }

    @Operation(
            summary = "Detalle categoría (admin)",
            description = "Devuelve el detalle de una categoría (incluye si está activa o no). Solo ADMIN."
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Detalle de categoría",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleCategoriaProducto.class),
                    examples = @ExampleObject(value = """
                    { "id": 1, "nombre": "Alimentos", "activo": true }
                    """)
            )
    )
    @GetMapping("/categorias/{id}")
    public ResponseEntity<DatosDetalleCategoriaProducto> detalleCategoriaAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalleCategoriaAdmin(id));
    }
}
