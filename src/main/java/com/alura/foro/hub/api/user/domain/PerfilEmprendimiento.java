package com.alura.foro.hub.api.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "perfil_emprendimiento",
        indexes = {
                @Index(name = "idx_emprendimiento_localidad", columnList = "localidad_id"),
                @Index(name = "idx_emprendimiento_activo", columnList = "activo")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PerfilEmprendimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 usuario = 1 emprendimiento por ahora
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Column(name = "logo_key", length = 400)
    private String logoKey;

    @Column(name = "telefono_contacto", length = 30)
    private String telefonoContacto;

    @Column(length = 120)
    private String instagram;

    @Column(length = 200)
    private String facebook;

    @Column(nullable = false, length = 80)
    private String provincia = "Santa Fe";

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;

    @Column(length = 120)
    private String direccion;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // NULL hasta primer edición
    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
        if (this.provincia == null || this.provincia.isBlank()) this.provincia = "Santa Fe";
        // fechaActualizacion queda NULL
    }

    @PreUpdate
    void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
