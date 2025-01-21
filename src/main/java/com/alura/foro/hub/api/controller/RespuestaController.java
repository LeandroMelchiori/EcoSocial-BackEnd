package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Topico;
import com.alura.foro.hub.api.service.RespuestaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @GetMapping
    public ResponseEntity<List<Respuesta>> listarRespuestas() {
        return ResponseEntity.ok(respuestaService.listarRespuestas());
    }

    @GetMapping("/topico/{topicoId}")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasPorTopico(@PathVariable Long topicoId) {
        Topico topico = new Topico();
        topico.setId(topicoId);
        return ResponseEntity.ok(respuestaService.obtenerPorTopico(topico));
    }

    @PostMapping
    public ResponseEntity<Respuesta> crearRespuesta(@RequestBody @Valid Respuesta respuesta) {
        return ResponseEntity.ok(respuestaService.crearRespuesta(respuesta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Respuesta> actualizarRespuesta(@PathVariable Long id, @RequestBody @Valid Respuesta respuesta) {
        return ResponseEntity.ok(respuestaService.actualizarRespuesta(id, respuesta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable Long id) {
        respuestaService.eliminarRespuesta(id);
        return ResponseEntity.noContent().build();
    }
}
