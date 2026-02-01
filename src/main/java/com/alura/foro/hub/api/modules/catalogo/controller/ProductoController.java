package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/catalogo/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CurrentUserService currentUser;

    public ProductoController(ProductoService productoService, CurrentUserService currentUser) {
        this.productoService = productoService;
        this.currentUser = currentUser;
    }

    // ✅ CREAR PRODUCTO (multipart)
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Crear producto",
            description = """
                    Crea un producto del catálogo.
                    Se envía como multipart/form-data con:
                    - data: JSON (DatosCrearProducto)
                    - imagenes: lista opcional de imágenes
                    """
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "201",
            description = "Producto creado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleProducto.class),
                    examples = @ExampleObject(
                            name = "Producto creado",
                            value = """
                                    {
                                      "id": 10,
                                      "emprendimientoId": 3,
                                      "categoriaId": 2,
                                      "subcategoriaId": 5,
                                      "titulo": "Pan casero integral",
                                      "descripcion": "Pan artesanal elaborado con masa madre.",
                                      "activo": true,
                                      "fechaCreacion": "2026-01-26T10:30:00",
                                      "imagenes": []
                                    }
                                    """
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> crear(
            @Parameter(
                    description = "Datos del producto en formato JSON",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DatosCrearProducto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "categoriaCatalogoId": 2,
                                              "subCategoriaCatalogoId": 5,
                                              "titulo": "Pan casero integral",
                                              "descripcion": "Pan artesanal elaborado con masa madre."
                                            }
                                            """
                            )
                    )
            )
            @RequestPart("data") @Valid DatosCrearProducto data,

            @Parameter(
                    description = "Imágenes del producto (opcional)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(name = "imagenes", required = false) List<MultipartFile> imagenes,

            Authentication auth
    ) throws IOException {

        Long userId = currentUser.userId(auth);

        DatosDetalleProducto creado = productoService.crear(data, imagenes, userId);

        URI location = URI.create("/catalogo/productos/" + creado.id());
        return ResponseEntity.created(location).body(creado);
    }

    // ✅ DETALLE
    @Operation(
            summary = "Detalle de producto",
            description = "Devuelve el detalle completo de un producto (incluye imágenes)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle del producto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DatosDetalleProducto.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleProducto> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.detalle(id));
    }

    // ✅ LISTAR + FILTROS + PAGINADO
    @Operation(
            summary = "Listar productos",
            description = """
                    Lista productos con paginación y filtros opcionales:
                    - categoriaId
                    - subcategoriaId
                    - q (búsqueda por texto)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado paginado de productos",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<Page<DatosListadoProducto>> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long subcategoriaId,
            @RequestParam(required = false) String q,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.listar(categoriaId, subcategoriaId, q, pageable));
    }

    // ✅ ELIMINAR
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto si el usuario autenticado es su autor (o admin si tu lógica lo contempla)."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        Long userId = currentUser.userId(auth);
        productoService.eliminar(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ✅ ACTUALIZAR (multipart)
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Actualizar producto",
            description = """
                    Actualiza los datos del producto y opcionalmente permite enviar nuevas imágenes.
                    multipart/form-data:
                    - data: JSON (DatosActualizarProducto)
                    - imagenes: opcional
                    """
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Producto actualizado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosDetalleProducto.class)
            )
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> actualizar(
            @PathVariable Long id,

            @Parameter(
                    description = "Datos del producto en formato JSON",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DatosActualizarProducto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "categoriaCatalogoId": 2,
                                              "subCategoriaCatalogoId": 5,
                                              "titulo": "Pan casero integral (actualizado)",
                                              "descripcion": "Ahora también disponible en versión con semillas."
                                            }
                                            """
                            )
                    )
            )
            @RequestPart("data") @Valid DatosActualizarProducto data,

            @Parameter(
                    description = "Imágenes nuevas (opcional)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,

            Authentication auth
    ) throws IOException {

        Long userId = currentUser.userId(auth);
        return ResponseEntity.ok(productoService.actualizar(id, data, imagenes, userId));
    }

    // ✅ REORDENAR IMÁGENES
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Reordenar imágenes de un producto",
            description = "Cambia el orden de las imágenes del producto."
    )
    @ApiResponsesDefault
    @PutMapping("/{id}/imagenes/orden")
    public ResponseEntity<DatosDetalleProducto> reordenarImagenes(
            @PathVariable Long id,
            @RequestBody @Valid DatosReordenarImagenes dto,
            Authentication auth
    ) {
        Long userId = currentUser.userId(auth);
        return ResponseEntity.ok(productoService.reordenarImagenes(id, dto, userId));
    }

    // ✅ ELIMINAR UNA IMAGEN
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Eliminar una imagen",
            description = "Elimina una imagen específica del producto."
    )
    @ApiResponsesDefault
    @ApiResponse(responseCode = "204", description = "Imagen eliminada")
    @DeleteMapping("/{productoId}/imagenes/{imagenId}")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long productoId,
            @PathVariable Long imagenId,
            Authentication auth
    ) {
        Long userId = currentUser.userId(auth);
        productoService.eliminarImagen(productoId, imagenId, userId);
        return ResponseEntity.noContent().build();
    }

    // ✅ REEMPLAZAR UNA IMAGEN
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Reemplazar una imagen",
            description = "Reemplaza una imagen existente manteniendo su posición/orden."
    )
    @ApiResponsesDefault
    @PutMapping(value = "/{productoId}/imagenes/{imagenId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> reemplazarImagen(
            @PathVariable Long productoId,
            @PathVariable Long imagenId,
            @RequestPart("imagen") MultipartFile imagen,
            Authentication auth
    ) throws IOException {
        Long userId = currentUser.userId(auth);
        return ResponseEntity.ok(productoService.reemplazarImagen(productoId, imagenId, imagen, userId));
    }

    // ✅ AGREGAR IMÁGENES
    @SecurityRequirement(name = "bearer-key")
    @Operation(
            summary = "Agregar imágenes a un producto",
            description = "Agrega imágenes sin modificar las existentes."
    )
    @ApiResponsesDefault
    @PostMapping(value = "/{productoId}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> agregarImagenes(
            @PathVariable Long productoId,
            @RequestPart("imagenes") List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {
        Long userId = currentUser.userId(auth);
        return ResponseEntity.ok(productoService.agregarImagenes(productoId, imagenes, userId));
    }
}
