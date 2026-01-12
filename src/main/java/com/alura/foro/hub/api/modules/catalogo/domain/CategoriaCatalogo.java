package com.alura.foro.hub.api.modules.catalogo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CategoriaCatalogo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;
}
