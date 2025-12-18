package com.alura.foro.hub.api.mapper;

import com.alura.foro.hub.api.entity.model.Categoria;
import com.alura.foro.hub.api.dto.categoria.DatosListadoCategoria;

public class CategoriaMapper {

    private CategoriaMapper() {
        // evitar instanciación
    }

    public static DatosListadoCategoria toListado(Categoria c) {
        return new DatosListadoCategoria(
                c.getId(),
                c.getNombre()
        );
    }
}
