package com.alura.foro.hub.api.user.dto.emprendimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DatosCrearEmprendimiento(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 2000) String descripcion,

        @Size(max = 30) String telefonoContacto,
        @Size(max = 120) String instagram,
        @Size(max = 200) String facebook,

        @Size(max = 80) String provincia,         // opcional, default Santa Fe en entidad
        @NotNull Long localidadId,                // OBLIGATORIO por tu entidad/tabla
        @Size(max = 120) String direccion,
        @Size(max = 10) String codigoPostal
) {}
