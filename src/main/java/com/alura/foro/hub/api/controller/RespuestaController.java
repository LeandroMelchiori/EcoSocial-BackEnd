package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.domain.Respuesta;
import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosActualizarRespuesta;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosCrearRespuesta;
import com.alura.foro.hub.api.domain.dto.respuesta.DatosListadoRespuesta;
import com.alura.foro.hub.api.service.RespuestaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;
    public RespuestaController(
            RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @PostMapping
    public ResponseEntity<DatosListadoRespuesta> crear(
            @RequestBody @Valid DatosCrearRespuesta datos,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(respuestaService.crear(datos, usuario.getId())
        );
    }

    @GetMapping("/topico/{topicoId}")
    public ResponseEntity<Page<DatosListadoRespuesta>> listar(
            @PathVariable Long topicoId,
            Pageable pageable) {
        return ResponseEntity.ok(respuestaService.listarPorTopico(topicoId, pageable));
    }

    @PatchMapping("/{id}/solucion")
    public ResponseEntity<DatosListadoRespuesta> marcarSolucion(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(
                respuestaService.marcarSolucion(id, usuario.getId())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosListadoRespuesta> editar(
            @PathVariable Long id,
            @RequestBody @Valid DatosActualizarRespuesta datos,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(respuestaService.actualizar(id, datos, usuario.getId())
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        respuestaService.eliminar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
