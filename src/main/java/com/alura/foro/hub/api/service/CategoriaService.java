package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.*;
import com.alura.foro.hub.api.domain.dto.categoria.DatosActualizarCategoria;
import com.alura.foro.hub.api.domain.dto.categoria.DatosCrearCategoria;
import com.alura.foro.hub.api.domain.dto.categoria.DatosListadoCategoria;
import com.alura.foro.hub.api.domain.dto.curso.DatosListadoCurso;
import com.alura.foro.hub.api.repository.CategoriaRepository;
import com.alura.foro.hub.api.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .map(c -> new DatosListadoCategoria(c.getId(), c.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DatosListadoCurso> listarCursosDeCategoria(Long categoriaId) {
        // valida que exista (para devolver 404 si no existe)
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        return cursoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public DatosListadoCategoria crear(DatosCrearCategoria datos) {
        if (categoriaRepository.existsByNombreIgnoreCase(datos.nombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        var cat = new Categoria();
        cat.setNombre(datos.nombre().trim());

        cat = categoriaRepository.save(cat);
        return new DatosListadoCategoria(cat.getId(), cat.getNombre());
    }

    @Transactional
    public DatosListadoCategoria actualizar(Long id, DatosActualizarCategoria datos) {
        var cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // Evitar duplicados al renombrar
        var nuevoNombre = datos.nombre().trim();
        if (!cat.getNombre().equalsIgnoreCase(nuevoNombre)
                && categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }

        cat.setNombre(nuevoNombre);

        return new DatosListadoCategoria(cat.getId(), cat.getNombre());
    }

    @Transactional
    public void eliminar(Long id) {
        var cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // Regla recomendada: si tiene cursos, no borrar
        if (cat.getCursos() != null && !cat.getCursos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede borrar la categoría porque tiene cursos asociados");
        }

        categoriaRepository.delete(cat);
    }



    private DatosListadoCurso toDTO(Curso c) {
        return new DatosListadoCurso(
                c.getId(),
                c.getNombre(),
                c.getCategoria().getId(),
                c.getCategoria().getNombre()
        );
    }
}
