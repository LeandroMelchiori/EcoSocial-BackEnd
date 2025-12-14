package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.domain.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.domain.dto.topico.DatosDetalleTopico;
import com.alura.foro.hub.api.domain.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.domain.dto.topico.DatosRegistroTopico;
import com.alura.foro.hub.api.service.TopicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<DatosListadoTopico>> listar(Pageable pageable) {
        return ResponseEntity.ok(topicoService.listar(pageable));
    }

    // CREAR
    @PostMapping
    public ResponseEntity crear(
            @RequestBody DatosRegistroTopico datos,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        Topico topico = topicoService.crearTopico(datos, userId);

        return ResponseEntity.ok(new DatosDetalleTopico(topico));
    }

    // ✏️ ACTUALIZAR POR ID
    @PutMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarTopico datos
    ) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var usuario = (Usuario) auth.getPrincipal();   // ✅
        Long usuarioId = usuario.getId();


        var dto = topicoService.actualizarTopico(id, datos, usuarioId);
        return ResponseEntity.ok(dto);
    }

    // 🗑️ ELIMINAR POR ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var usuario = (Usuario) auth.getPrincipal();
        topicoService.eliminarTopico(id, usuario);
        return ResponseEntity.noContent().build();
    }

    // DETALLAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<DatosDetalleTopico> detallar(@PathVariable Long id) {
        DatosDetalleTopico dto = topicoService.detallarTopico(id);
        return ResponseEntity.ok(dto);
    }

}
