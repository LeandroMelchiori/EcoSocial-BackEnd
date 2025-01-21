package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.service.TopicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicoService topicoService;

    public TopicoController(TopicoService topicoService) {
        this.topicoService = topicoService;
    }

    @GetMapping
    public ResponseEntity<List<DatosListadoTopico>> listar() {
        return ResponseEntity.ok(topicoService.listarTopicos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> obtenerPorId(@PathVariable Long id) {
        Optional<DatosDetalleTopico> topico = topicoService.obtenerPorId(id);
        return topico.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Topico> crear(@RequestBody DatosRegistroTopico datos) {
        Topico nuevoTopico = topicoService.crearTopico(datos);
        return ResponseEntity.ok(nuevoTopico);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topico> actualizar(@PathVariable Long id, @RequestBody DatosActualizarTopico datos) {
        return ResponseEntity.ok(topicoService.actualizarTopico(id, datos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        topicoService.eliminarTopico(id);
        return ResponseEntity.noContent().build();
    }
}

