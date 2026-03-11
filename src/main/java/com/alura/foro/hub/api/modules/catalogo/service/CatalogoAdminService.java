package com.alura.foro.hub.api.modules.catalogo.service;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import com.alura.foro.hub.api.modules.catalogo.dto.categorias.*;
import com.alura.foro.hub.api.modules.catalogo.dto.subcategorias.*;
import com.alura.foro.hub.api.modules.catalogo.repository.*;
import com.alura.foro.hub.api.security.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogoAdminService {

    private final CategoriaCatalogoRepository categoriaRepo;
    private final SubCategoriaCatalogoRepository subcategoriaRepo;
    private final ProductoRepository productoRepo;

    public CatalogoAdminService(CategoriaCatalogoRepository categoriaRepo,
                                SubCategoriaCatalogoRepository subcategoriaRepo,
                                ProductoRepository productoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.subcategoriaRepo = subcategoriaRepo;
        this.productoRepo = productoRepo;
    }

    // ---------- CATEGORIAS ----------

    @Transactional
    public DatosDetalleCategoriaProducto crearCategoria(DatosCrearCategoriaProducto dto) {
        String nombre = dto.nombre().trim();

        if (categoriaRepo.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        CategoriaCatalogo c = new CategoriaCatalogo();
        c.setNombre(nombre);
        c.setActivo(true);

        c = categoriaRepo.save(c);
        return new DatosDetalleCategoriaProducto(c.getId(), c.getNombre(), c.getActivo());
    }

    @Transactional
    public DatosDetalleCategoriaProducto actualizarCategoria(Long id, DatosActualizarCategoriaProducto dto) {
        CategoriaCatalogo c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        String nombre = dto.nombre().trim();

        // evitar colisión por nombre (si cambia)
        categoriaRepo.findByNombreIgnoreCase(nombre).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new ConflictException("Ya existe una categoría con ese nombre");
            }
        });

        c.setNombre(nombre);
        c = categoriaRepo.save(c);

        return new DatosDetalleCategoriaProducto(c.getId(), c.getNombre(), c.getActivo());
    }

    @Transactional
    public void activarCategoria(Long id) {
        CategoriaCatalogo c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        c.setActivo(true);
    }

    @Transactional
    public void desactivarCategoria(Long id) {
        CategoriaCatalogo c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        c.setActivo(false);
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        // si hay productos o subcategorias, no permitimos (evita FK error)
        if (productoRepo.existsByCategoria_Id(id)) {
            throw new ConflictException("No se puede eliminar: hay productos asociados");
        }
        if (subcategoriaRepo.existsByCategoria_Id(id)) {
            throw new ConflictException("No se puede eliminar: hay subcategorías asociadas");
        }

        CategoriaCatalogo c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        categoriaRepo.delete(c);
    }

    // ---------- SUBCATEGORIAS ----------

    @Transactional
    public DatosDetalleSubcategoriaProducto crearSubcategoria(DatosCrearSubcategoriaProducto dto) {
        Long categoriaId = dto.categoriaId();
        String nombre = dto.nombre().trim();

        CategoriaCatalogo categoria = categoriaRepo.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        if (subcategoriaRepo.existsByCategoria_IdAndNombreIgnoreCase(categoriaId, nombre)) {
            throw new ConflictException("Ya existe esa subcategoría en la categoría indicada");
        }

        Subcategoria s = new Subcategoria();
        s.setCategoria(categoria);
        s.setNombre(nombre);
        s.setActivo(true);

        s = subcategoriaRepo.save(s);
        return new DatosDetalleSubcategoriaProducto(s.getId(), categoriaId, s.getNombre(), s.getActivo());
    }

    @Transactional
    public DatosDetalleSubcategoriaProducto actualizarSubcategoria(Long id, DatosActualizarSubcategoriaProducto dto) {
        Subcategoria s = subcategoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));

        Long categoriaId = dto.categoriaId();
        String nombre = dto.nombre().trim();

        CategoriaCatalogo categoria = categoriaRepo.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // si cambia categoria o nombre, validar unicidad
        subcategoriaRepo.findByCategoria_IdAndNombreIgnoreCase(categoriaId, nombre).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new ConflictException("Ya existe esa subcategoría en la categoría indicada");
            }
        });

        s.setCategoria(categoria);
        s.setNombre(nombre);

        s = subcategoriaRepo.save(s);
        return new DatosDetalleSubcategoriaProducto(s.getId(), categoriaId, s.getNombre(), s.getActivo());
    }

    @Transactional
    public void activarSubcategoria(Long id) {
        Subcategoria s = subcategoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));
        s.setActivo(true);
    }

    @Transactional
    public void desactivarSubcategoria(Long id) {
        Subcategoria s = subcategoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));
        s.setActivo(false);
    }

    @Transactional
    public void eliminarSubcategoria(Long id) {
        if (productoRepo.existsBySubcategoria_Id(id)) {
            throw new ConflictException("No se puede eliminar: hay productos asociados");
        }

        Subcategoria s = subcategoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));

        subcategoriaRepo.delete(s);
    }

    @Transactional(readOnly = true)
    public List<DatosDetalleCategoriaProducto> listarCategoriasAdmin() {
        return categoriaRepo.findAllByOrderByNombreAsc()
                .stream()
                .map(c -> new DatosDetalleCategoriaProducto(c.getId(), c.getNombre(), c.getActivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DatosDetalleSubcategoriaProducto> listarSubcategoriasAdmin(Long categoriaId) {
        var lista = (categoriaId == null)
                ? subcategoriaRepo.findAllByOrderByNombreAsc()
                : subcategoriaRepo.findByCategoria_IdOrderByNombreAsc(categoriaId);

        return lista.stream()
                .map(s -> new DatosDetalleSubcategoriaProducto(
                        s.getId(),
                        s.getCategoria().getId(),
                        s.getNombre(),
                        s.getActivo()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DatosDetalleSubcategoriaProducto detalleSubcategoriaAdmin(Long id) {
        Subcategoria s = subcategoriaRepo.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subcategoría no encontrada"));

        return new DatosDetalleSubcategoriaProducto(
                s.getId(),
                s.getCategoria().getId(),
                s.getNombre(),
                s.getActivo()
        );
    }

    @Transactional(readOnly = true)
    public DatosDetalleCategoriaProducto detalleCategoriaAdmin(Long id) {
        CategoriaCatalogo c = categoriaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        return new DatosDetalleCategoriaProducto(c.getId(), c.getNombre(), c.getActivo());
    }

}
