package com.alura.foro.hub.api.modules.catalogo.unit.emprendimiento;

import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import com.alura.foro.hub.api.user.mapper.EmprendimientoMapper;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.user.service.EmprendimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmprendimientoServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PerfilEmprendimientoRepository perfilRepository;
    @Mock LocalidadRepository localidadRepository;
    @Mock ProductoRepository productoRepository;
    @Mock CurrentUserService currentUser;
    @Mock StorageService storageService;
    @Mock EmprendimientoMapper mapper;

    @Mock Authentication auth;

    @InjectMocks
    EmprendimientoService service;

    @BeforeEach
    void setupAuth() {
        when(auth.isAuthenticated()).thenReturn(true);
        when(currentUser.userId(auth)).thenReturn(10L);
    }

    @Test
    void crear_ok() {
        var user = new Usuario();
        user.setId(10L);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(user));
        when(perfilRepository.existsByUsuarioId(10L)).thenReturn(false);

        var loc = new Localidad();
        loc.setId(1L);
        loc.setNombre("Rosario");
        loc.setActivo(true);
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(loc));

        var dto = new DatosCrearEmprendimiento(
                "Panadería", "desc", "341", "ig", "fb",
                "Santa Fe", 1L, "Calle falsa 123", "2000"
        );

        // mapper: construir entidad
        PerfilEmprendimiento entidad = new PerfilEmprendimiento();
        entidad.setUsuario(user);
        entidad.setLocalidad(loc);
        entidad.setNombre(dto.nombre());
        when(mapper.toEntity(dto, user, loc)).thenReturn(entidad);

        // al guardar devolvés entidad con id
        when(perfilRepository.save(any())).thenAnswer(inv -> {
            PerfilEmprendimiento pe = inv.getArgument(0);
            pe.setId(99L);
            pe.setActivo(true);
            return pe;
        });

        var detalle = new DatosDetalleEmprendimiento(
                99L, 10L, "Panadería", "desc",
                null, null, "341", "ig", "fb",
                "Santa Fe", 1L, "Rosario", "Calle falsa 123", "2000", true
        );
        when(mapper.toDetalle(any())).thenReturn(detalle);

        var res = service.crearMiEmprendimiento(auth, dto);

        assertEquals(99L, res.id());
        assertEquals(10L, res.usuarioId());
        assertEquals("Santa Fe", res.provincia());
        assertTrue(res.activo());
    }
}
