package com.alura.foro.hub.api.user.dto.emprendimiento;

import java.time.LocalDateTime;

public record DatosDetalleEmprendimiento(
        Long id,
        Long usuarioId,
        String nombre,
        String descripcion,
        String logoKey,
        String telefonoContacto,
        String instagram,
        String facebook,
        String provincia,
        Long localidadId,
        String localidadNombre,
        String direccion,
        String codigoPostal,
        Boolean activo
) {}
