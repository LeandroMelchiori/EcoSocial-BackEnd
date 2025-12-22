package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.mapper.CategoriaMapper;
import com.alura.foro.hub.api.mapper.CursoMapper;
import com.alura.foro.hub.api.repository.CategoriaRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import com.alura.foro.hub.api.security.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CursoRepository cursoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, CursoRepository cursoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.cursoRepository = cursoRepository;
    }

    public List<DatosListadoCategoria> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaMapper::toListado)
                .toList();

    }

    @Transactional(readOnly = true)
    public List<DatosListadoCurso> listarCursosDeCategoria(Long categoriaId) {
        // valida que exista (para devolver 404 si no existe)
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        return cursoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(CursoMapper::toListado)
                .toList();
    }

    @Transactional
    public DatosListadoCategoria crear(DatosCrearCategoria datos) {
        if (categoriaRepository.existsByNombreIgnoreCase(datos.nombre())) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }

        var cat = CategoriaMapper.fromCrear(datos);
        categoriaRepository.save(cat);

        return CategoriaMapper.toListado(cat);
    }


    @Transactional
    public DatosListadoCategoria actualizar(Long id, DatosActualizarCategoria datos) {
        var cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // Evitar duplicados al renombrar
        var nuevoNombre = datos.nombre().trim();
        if (!cat.getNombre().equalsIgnoreCase(nuevoNombre)
                && categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }

        cat.setNombre(nuevoNombre);

        return CategoriaMapper.toListado(cat);
    }

    @Transactional
    public void eliminar(Long id) {
        var cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // Regla recomendada: si tiene cursos, no borrar
        if (cat.getCursos() != null && !cat.getCursos().isEmpty()) {
            throw new BusinessException("No se puede borrar la categoría porque tiene cursos asociados");
        }

        categoriaRepository.delete(cat);
    }
}
