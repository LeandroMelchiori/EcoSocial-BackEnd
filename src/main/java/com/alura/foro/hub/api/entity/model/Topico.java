package com.alura.foro.hub.api.entity.model;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.dto.topico.DatosActualizarTopico;
import com.alura.foro.hub.api.dto.topico.DatosRegistroTopico;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Topico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String mensaje;

    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    private StatusTopico status = StatusTopico.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @OneToMany(mappedBy = "topico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Respuesta> respuestas = new ArrayList<>();

    // Constructor desde DTO
    public Topico(DatosRegistroTopico datos, Usuario autor, Curso curso) {
        this.titulo = datos.titulo();
        this.mensaje = datos.mensaje();
        this.fechaCreacion = LocalDateTime.now();
        this.autor = autor;
        this.curso = curso;
        this.status = StatusTopico.ACTIVO; // o el que quieras por defecto
    }

    public void actualizar(String titulo, String mensaje, StatusTopico status) {
        if (titulo != null) this.titulo = titulo;
        if (mensaje != null) this.mensaje = mensaje;
        if (status != null) this.status = status;
    }

    // Cerrar topico
    public void cerrarTopico() {
        this.status = StatusTopico.CERRADO;
    }

    // Topico solucionadi
    public void solucionado() {
        this.status = StatusTopico.SOLUCIONADO;
    }

    public void reactivarTopico() {
        this.status = StatusTopico.ACTIVO;
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (status == null) status = StatusTopico.ACTIVO;
    }
}
