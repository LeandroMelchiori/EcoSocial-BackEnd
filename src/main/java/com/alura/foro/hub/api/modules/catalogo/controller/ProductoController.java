package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.*;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
public class    ProductoController {

    private final ProductoService productoService;
    private final ObjectMapper mapper;
    private final CurrentUserService currentUser;

    public ProductoController(ProductoService productoService, ObjectMapper mapper, CurrentUserService currentUser) {
        this.productoService = productoService;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crear(
            @RequestPart("data") String data,
            @RequestPart(name = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {

        DatosCrearProducto dto = mapper.readValue(data, DatosCrearProducto.class);

        Long userId = currentUser.userId(auth);

        DatosDetalleProducto creado = productoService.crear(dto, imagenes, userId);

        URI location = URI.create("/catalogo/productos/" + creado.id());
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleProducto> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.detalle(id));
    }

    @GetMapping
    public ResponseEntity<Page<DatosListadoProducto>> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long subcategoriaId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(productoService.listar(categoriaId, subcategoriaId, q, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        productoService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> actualizar(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {
        var dto = mapper.readValue(data, DatosActualizarProducto.class);
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.actualizar(id, dto, imagenes, usuario.getId()));
    }

    @PutMapping("/{id}/imagenes/orden")
    public ResponseEntity<DatosDetalleProducto> reordenarImagenes(
            @PathVariable Long id,
            @RequestBody DatosReordenarImagenes dto,
            Authentication auth
    ) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.reordenarImagenes(id, dto, usuario.getId()));
    }

    // DELETE una imagen
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

    // REEMPLAZAR una imagen (misma posición/orden)
    @PutMapping(value = "/{productoId}/imagenes/{imagenId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> reemplazarImagen(
            @PathVariable Long productoId,
            @PathVariable Long imagenId,
            @RequestPart("imagen") MultipartFile imagen,
            Authentication auth
    ) throws IOException {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.reemplazarImagen(productoId, imagenId, imagen, usuario.getId()));
    }

    // AGREGAR imágenes (sin tocar las existentes)
    @PostMapping(value = "/{productoId}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> agregarImagenes(
            @PathVariable Long productoId,
            @RequestPart("imagenes") List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(productoService.agregarImagenes(productoId, imagenes, usuario.getId()));
    }

}
