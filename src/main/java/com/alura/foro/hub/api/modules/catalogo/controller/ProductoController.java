package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/catalogo/productos")
public class    ProductoController {

    private final ProductoService productoService;
    private final ObjectMapper mapper;
    private final CurrentUserService currentUser;

    public ProductoController(ProductoService productoService, ObjectMapper mapper, CurrentUserService currentUser) {
        this.productoService = productoService;
        this.mapper = mapper;
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
                          "usuarioId": 3,
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
    public ResponseEntity<?> crear(
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
            summary = "Listar productos",
            description = """
        Lista productos del catálogo con filtros opcionales:
        - categoriaId
        - subcategoriaId
        - q (búsqueda por título)
        """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado paginado de productos",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                        {
                          "content": [
                            {
                              "id": 10,
                              "titulo": "Pan casero integral",
                              "activo": true,
                              "fechaCreacion": "2026-01-26T10:30:00",
                              "usuarioId": 3,
                              "categoriaId": 2,
                              "subcategoriaId": 5,
                              "thumbnailUrl": "https://cdn.midominio.com/productos/pan-thumb.jpg"
                            }
                          ],
                          "totalPages": 1,
                          "totalElements": 1,
                          "size": 10,
                          "number": 0
                        }
                        """
                    )
            )
    )
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
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Página de productos",
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "id": 10,
                                          "nombre": "Pan casero",
                                          "precio": 2500.0,
                                          "categoriaId": 2,
                                          "subcategoriaId": 5,
                                          "urlImagenPrincipal": "https://.../pan-1.jpg"
                                        }
                                      ],
                                      "totalPages": 1,
                                      "totalElements": 1,
                                      "size": 10,
                                      "number": 0
                                    }
                                    """
                            )
                    )
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
        Usuario usuario = (Usuario) auth.getPrincipal();
        productoService.eliminar(id, usuario.getId());
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
                    schema = @Schema(implementation = DatosDetalleProducto.class),
                    examples = @ExampleObject(
                            name = "Actualización exitosa",
                            value = """
                            {
                              "id": 10,
                              "nombre": "Pan casero (nuevo)",
                              "descripcion": "Ahora también integral",
                              "precio": 2800.0,
                              "categoriaId": 2,
                              "subcategoriaId": 5,
                              "imagenes": [
                                { "id": 100, "url": "https://.../pan-1.jpg", "orden": 0 }
                              ],
                              "fechaCreacion": "2026-01-26T10:00:00"
                            }
                            """
                    )
            )
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> actualizar(
            @PathVariable Long id,
            @Parameter(
                    description = "JSON del producto (DatosActualizarProducto) como string",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DatosActualizarProducto.class),
                            examples = @ExampleObject(
                                    name = "Data",
                                    value = """
                                    {
                                      "nombre": "Pan casero (nuevo)",
                                      "descripcion": "Ahora también integral",
                                      "precio": 2800,
                                      "categoriaId": 2,
                                      "subcategoriaId": 5
                                    }
                                    """
                            )
                    )
            )
            @RequestPart("data") String data,
            @Parameter(
                    description = "Imágenes nuevas (opcional)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {
        var dto = mapper.readValue(data, DatosActualizarProducto.class);
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.actualizar(id, dto, imagenes, usuario.getId()));
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
            @RequestBody DatosReordenarImagenes dto,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.reordenarImagenes(id, dto, usuario.getId()));
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
        Usuario usuario = (Usuario) auth.getPrincipal();
        productoService.eliminarImagen(productoId, imagenId, usuario.getId());
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
            @Parameter(
                    description = "Archivo de imagen",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("imagen") MultipartFile imagen,
            Authentication auth
    ) throws IOException {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.reemplazarImagen(productoId, imagenId, imagen, usuario.getId()));
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
            @Parameter(
                    description = "Lista de imágenes",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("imagenes") List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.agregarImagenes(productoId, imagenes, usuario.getId()));
    }

}
