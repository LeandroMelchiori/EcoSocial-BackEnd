// domain/Respuesta.java
package com.alura.foro.hub.api.entity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "respuesta")
public class Respuesta extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topico_id", nullable = false)
    private Topico topico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean solucion = false;

    @Column(nullable = false)
    private Boolean editado = false;

    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion DESC")
    private List<RespuestaHija> respuestasHijas = new ArrayList<>();


    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (solucion == null) solucion = false;
        if (editado == null) editado = false;
    }
}
