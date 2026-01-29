package com.alura.foro.hub.api.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "localidad",
        indexes = {
                @Index(name = "idx_localidad_nombre", columnList = "nombre")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Localidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "georef_id", nullable = false, unique = true, length = 20)
    private String georefId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String departamento;

    private Double lat;
    private Double lon;

    @Column(nullable = false)
    private Boolean activo = true;
}

