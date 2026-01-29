package com.alura.foro.hub.api.modules.catalogo.service;

import com.alura.foro.hub.api.modules.catalogo.dto.categorias.DatosDetalleCategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.DatosDetalleSubcategoriaProducto;
import com.alura.foro.hub.api.modules.catalogo.repository.CategoriaCatalogoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.SubCategoriaCatalogoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaCatalogoService {

    private final CategoriaCatalogoRepository categoriaRepo;
    private final SubCategoriaCatalogoRepository subcategoriaRepo;

    public CategoriaCatalogoService(CategoriaCatalogoRepository categoriaRepo,
                                    SubCategoriaCatalogoRepository subcategoriaRepo) {
        this.categoriaRepo = categoriaRepo;
        this.subcategoriaRepo = subcategoriaRepo;
    }

    @Transactional(readOnly = true)
    public List<DatosDetalleCategoriaProducto> listarCategoriasActivas() {
        return categoriaRepo.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(c -> new DatosDetalleCategoriaProducto(c.getId(), c.getNombre(), c.getActivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DatosDetalleSubcategoriaProducto> listarSubcategoriasActivas(Long categoriaId) {

        if (!categoriaRepo.existsByIdAndActivoTrue(categoriaId)) {
            throw new EntityNotFoundException("Categoría no encontrada");
        }

        return subcategoriaRepo.findByCategoria_IdAndActivoTrueOrderByNombreAsc(categoriaId)
                .stream()
                .map(s -> new DatosDetalleSubcategoriaProducto(
                        s.getId(),
                        s.getCategoria().getId(),
                        s.getNombre(),
                        s.getActivo()
                ))
                .toList();
    }
}
