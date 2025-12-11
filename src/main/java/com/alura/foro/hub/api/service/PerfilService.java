package com.alura.foro.hub.api.service;

import com.alura.foro.hub.api.domain.Perfil;
import com.alura.foro.hub.api.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;

    @Autowired
    public PerfilService(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    public List<Perfil> obtenerTodosLosPerfiles() {
        return perfilRepository.findAll();
    }

    public Optional<Perfil> obtenerPerfilPorId(Long id) {
        return perfilRepository.findById(id);
    }

    public Perfil crearPerfil(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    public Perfil actualizarPerfil(Long id, Perfil perfilActualizado) {
        if (perfilRepository.existsById(id)) {
            perfilActualizado.setId(id);
            return perfilRepository.save(perfilActualizado);
        }
        return null;
    }

    public void eliminarPerfil(Long id) {
        perfilRepository.deleteById(id);
    }
}
