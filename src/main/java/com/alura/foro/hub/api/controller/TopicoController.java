package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.DatosActualizarTopico;
import com.alura.foro.hub.api.domain.DatosDetalleTopico;
import com.alura.foro.hub.api.domain.DatosListadoTopico;
import com.alura.foro.hub.api.domain.DatosRegistroTopico;
import com.alura.foro.hub.api.service.TopicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicoService topicoService;

    public TopicoController(TopicoService topicoService) {
        this.topicoService = topicoService;
    }

    // LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<DatosListadoTopico>> listar() {
        var lista = topicoService.listar();
        return ResponseEntity.ok(lista);
    }

    // DETALLE POR ID
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> detalle(@PathVariable Long id) {
        var dto = topicoService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    // CREAR
    @PostMapping
    public ResponseEntity<DatosDetalleTopico> crear(@RequestBody @Valid DatosRegistroTopico datos) {
        var dto = topicoService.crear(datos);
        URI url = URI.create("/topicos/" + dto.id());
        return ResponseEntity.created(url).body(dto);
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> actualizar(@PathVariable Long id,
                                                         @RequestBody @Valid DatosActualizarTopico datos) {
        var dto = topicoService.actualizar(id, datos);
        return ResponseEntity.ok(dto);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        topicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
