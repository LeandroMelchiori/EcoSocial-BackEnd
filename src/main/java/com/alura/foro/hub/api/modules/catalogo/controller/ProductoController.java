package com.alura.foro.hub.api.modules.catalogo.controller;

import com.alura.foro.hub.api.modules.catalogo.domain.Producto;
import com.alura.foro.hub.api.modules.catalogo.dto.DatosCrearProducto;
import com.alura.foro.hub.api.modules.catalogo.service.ProductoService;
import com.alura.foro.hub.api.user.domain.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/catalogo/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crear(
            @RequestPart("data") @Valid DatosCrearProducto data,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Authentication auth
    ) throws IOException {

        Usuario usuario = (Usuario) auth.getPrincipal();
        var creado = productoService.crear(data, imagenes, usuario.getId());
        return ResponseEntity.ok(creado.getId());
    }

}
