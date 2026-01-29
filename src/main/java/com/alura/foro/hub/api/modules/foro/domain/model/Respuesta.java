// domain/Respuesta.java
package com.alura.foro.hub.api.modules.foro.domain.model;

import com.alura.foro.hub.api.user.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "respuesta")
public class Respuesta extends BaseEntity{

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

    @Column(nullable = false)
    private Boolean solucion = false;

    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion DESC")
    private List<RespuestaHija> respuestasHijas = new ArrayList<>();


    @PrePersist
    void prePersist() {
        if (solucion == null) solucion = false;
    }
}
