package com.alura.foro.hub.api.modules.catalogo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subcategoria_producto",
        uniqueConstraints = @UniqueConstraint(name="uq_subcategoria_producto", columnNames={"categoria_producto_id","nombre"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Subcategoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_producto_id", nullable = false)
    private CategoriaCatalogo categoria;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;
}
