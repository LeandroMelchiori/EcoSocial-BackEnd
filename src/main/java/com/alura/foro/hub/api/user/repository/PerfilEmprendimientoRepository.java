package com.alura.foro.hub.api.user.repository;

import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilEmprendimientoRepository extends JpaRepository<PerfilEmprendimiento, Long> {
    Optional<PerfilEmprendimiento> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}
