package com.alura.foro.hub.api.modules.foro.mapper;

import com.alura.foro.hub.api.modules.foro.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.modules.foro.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;
import com.alura.foro.hub.api.modules.foro.dto.categoria.DatosListadoCategoria;

public class CategoriaMapper {

    private CategoriaMapper() {}

    public static Categoria fromCrear(DatosCrearCategoria datos) {
        var c = new Categoria();
        c.setNombre(datos.nombre().trim());
        return c;
    }

    public static void aplicarActualizacion(Categoria c, DatosActualizarCategoria datos) {
        c.setNombre(datos.nombre().trim());
    }

    public static DatosListadoCategoria toListado(Categoria c) {
        return new DatosListadoCategoria(c.getId(), c.getNombre());
    }
}

