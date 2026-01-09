package com.alura.foro.hub.api.modules.catalogo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_imagenes",
        uniqueConstraints = @UniqueConstraint(name="uq_imagen_orden", columnNames={"producto_id","orden"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProductoImagen {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Integer orden;
}
