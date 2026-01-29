package com.alura.foro.hub.api.modules.foro.domain.model;

import com.alura.foro.hub.api.user.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "respuesta_hija")
public class RespuestaHija extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private Respuesta respuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;
}
