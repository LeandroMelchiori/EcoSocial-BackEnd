package com.alura.foro.hub.api.user.controller;

import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosActualizarEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import com.alura.foro.hub.api.user.service.EmprendimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/me/emprendimiento")
@Tag(name = "Emprendimiento (mi perfil)", description = "Endpoints del emprendimiento del usuario logueado.")
@SecurityRequirement(name = "bearer-key")
public class MiEmprendimientoController {

    private final EmprendimientoService emprendimientoService;

    public MiEmprendimientoController(EmprendimientoService emprendimientoService) {
        this.emprendimientoService = emprendimientoService;
    }

    @Operation(summary = "Crear mi emprendimiento",
            description = "Crea el emprendimiento asociado al usuario logueado (1 usuario = 1 emprendimiento).")
    @ApiResponsesDefault
    @PostMapping
    public ResponseEntity<DatosDetalleEmprendimiento> crear(
            Authentication auth,
            @RequestBody @Valid DatosCrearEmprendimiento dto
    ) {
        var creado = emprendimientoService.crearMiEmprendimiento(auth, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @Operation(summary = "Ver mi emprendimiento",
            description = "Devuelve el emprendimiento del usuario logueado.")
    @ApiResponsesDefault
    @GetMapping
    public ResponseEntity<DatosDetalleEmprendimiento> ver(Authentication auth) {
        return ResponseEntity.ok(emprendimientoService.verMiEmprendimiento(auth));
    }

    @Operation(summary = "Eliminar mi emprendimiento",
            description = "Elimina emprendimiento previamente habiendo eliminado los productos asociados.")
    @ApiResponsesDefault
    @DeleteMapping
    public ResponseEntity<Void> eliminar(Authentication auth) {
        emprendimientoService.eliminarMiEmprendimiento(auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Subir/Reemplazar logo",
            description = "Sube el logo del emprendimiento. Guarda en storage (MinIO/local) y actualiza logoKey.")
    @ApiResponsesDefault
    @PutMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatosDetalleEmprendimiento> subirLogo(
            Authentication auth,
            @RequestPart("logo") MultipartFile logo
    ) {
        return ResponseEntity.ok(emprendimientoService.subirLogo(auth, logo));
    }

    @Operation(summary = "Eliminar logo",
            description = "Elimina el logo del emprendimiento (borra del storage y limpia logoKey).")
    @ApiResponsesDefault
    @DeleteMapping("/logo")
    public ResponseEntity<Void> eliminarLogo(Authentication auth) {
        emprendimientoService.eliminarLogo(auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar mi emprendimiento",
            description = "Actualiza los datos del emprendimiento del usuario logueado.")
    @ApiResponsesDefault
    @PutMapping
    public ResponseEntity<DatosDetalleEmprendimiento> actualizar(
            Authentication auth,
            @RequestBody @Valid DatosActualizarEmprendimiento datos
    ) {
        return ResponseEntity.ok(emprendimientoService.actualizar(auth, datos));
    }
}
