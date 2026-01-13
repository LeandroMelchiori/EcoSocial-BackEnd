package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosCrearProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosActualizarProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosDetalleProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.productos.DatosListadoProducto;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class    ProductoController {

    private final ProductoService productoService;
    private final ObjectMapper mapper;

    public ProductoController(ProductoService productoService, ObjectMapper mapper) {
        this.productoService = productoService;
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crear(
            @RequestPart("data") String data,
            @RequestPart(name = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {

        DatosCrearProducto dto = mapper.readValue(data, DatosCrearProducto.class);

        Usuario usuario = (Usuario) auth.getPrincipal();

        DatosDetalleProducto creado = productoService.crear(dto, imagenes, usuario.getId());

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

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        productoService.eliminar(id, usuario.getId(), esAdmin);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleProducto> actualizar(
            @PathVariable Long id,
            @RequestPart("data") @Valid DatosActualizarProducto data,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {

        Usuario usuario = (Usuario) auth.getPrincipal();

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        var actualizado = productoService.actualizar(id, data, imagenes, usuario.getId(), esAdmin);

        return ResponseEntity.ok(actualizado);
    }

}
