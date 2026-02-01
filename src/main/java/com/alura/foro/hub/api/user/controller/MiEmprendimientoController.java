package com.alura.foro.hub.api.user.controller;

import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import com.alura.foro.hub.api.user.service.EmprendimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Emprendimiento (mi perfil)", description = "Endpoints del emprendimiento del usuario logueado.")
@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/me/emprendimiento")
public class MiEmprendimientoController {

    private final EmprendimientoService service;
    private final CurrentUserService currentUserService;

    public MiEmprendimientoController(EmprendimientoService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @Operation(summary = "Crear mi emprendimiento", description = "Crea el emprendimiento asociado al usuario logueado (1 usuario = 1 emprendimiento).")
    @ApiResponsesDefault
    @PostMapping
    public ResponseEntity<DatosDetalleEmprendimiento> crear(Authentication auth,
                                                            @RequestBody @Valid DatosCrearEmprendimiento dto) {
        Long userId = currentUserService.userId(auth);
        var creado = service.crearMiEmprendimiento(userId, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @Operation(summary = "Ver mi emprendimiento", description = "Devuelve el emprendimiento del usuario logueado.")
    @ApiResponsesDefault
    @GetMapping
    public ResponseEntity<DatosDetalleEmprendimiento> ver(Authentication auth) {
        Long userId = currentUserService.userId(auth);
        return ResponseEntity.ok(service.verMiEmprendimiento(userId));
    }


    @Operation(summary = "Eliminar mi emprendimiento", description = "Elimina emprendimiento previamente habiendo eliminado los productos asociados.")
    @ApiResponsesDefault
    @DeleteMapping
    public ResponseEntity<Void> eliminar(Authentication auth) {
        service.eliminarMiEmprendimiento(auth);
        return ResponseEntity.noContent().build(); // 204
    }
}
