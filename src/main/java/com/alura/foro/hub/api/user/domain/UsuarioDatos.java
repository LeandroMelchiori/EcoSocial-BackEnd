package com.alura.foro.hub.api.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuario_datos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UsuarioDatos {

    @Id
    private Long id; // mismo ID que Usuario

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(length = 120)
    private String direccion;

    @Column(length = 80)
    private String localidad;

    @Column(length = 80)
    private String provincia;

    @Column(length = 20, unique = true)
    private String dni;

    private LocalDate fechaNacimiento;
}
