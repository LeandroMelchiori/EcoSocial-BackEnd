package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.DatosActualizarRespuesta;
import com.alura.foro.hub.api.domain.DatosCrearRespuesta;
import com.alura.foro.hub.api.domain.DatosListadoRespuesta;
import com.alura.foro.hub.api.service.RespuestaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @PostMapping
    public ResponseEntity<DatosListadoRespuesta> crear(@RequestBody @Valid DatosCrearRespuesta datos,
                                                       HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(respuestaService.crear(datos, userId));
    }

    @GetMapping("/topico/{topicoId}")
    public ResponseEntity<Page<DatosListadoRespuesta>> listar(@PathVariable Long topicoId,
                                                              Pageable pageable) {
        return ResponseEntity.ok(respuestaService.listarPorTopico(topicoId, pageable));
    }

    @PatchMapping("/{id}/solucion")
    public ResponseEntity<DatosListadoRespuesta> marcarSolucion(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(respuestaService.marcarSolucion(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoRespuesta> editar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarRespuesta datos,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(respuestaService.actualizar(id, datos, userId));
    }

}
