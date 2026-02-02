package com.alura.foro.hub.api.modules.catalogo.unit.emprendimiento;

import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ConflictException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.mapper.EmprendimientoMapper;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.alura.foro.hub.api.user.service.EmprendimientoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmprendimientoServiceCrearUnitTest {

    PerfilEmprendimientoRepository emprendimientoRepository;
    UsuarioRepository usuarioRepository;
    LocalidadRepository localidadRepository;
    ProductoRepository productoRepository;
    CurrentUserService currentUser;
    StorageService storageService;
    EmprendimientoMapper mapper;

    EmprendimientoService service;

    Authentication auth;

    @BeforeEach
    void init() {
        emprendimientoRepository = mock(PerfilEmprendimientoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        localidadRepository = mock(LocalidadRepository.class);
        productoRepository = mock(ProductoRepository.class);
        currentUser = mock(CurrentUserService.class);
        storageService = mock(StorageService.class);
        mapper = mock(EmprendimientoMapper.class);

        service = new EmprendimientoService(
                emprendimientoRepository,
                usuarioRepository,
                localidadRepository,
                productoRepository,
                currentUser,
                storageService,
                mapper
        );

        auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true); // ✅ CLAVE: evita Forbidden en todos los tests
    }

    @Test
    void crear_authNull_forbidden() {
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 1L, null, null
        );

        assertThatThrownBy(() -> service.crearMiEmprendimiento(null, dto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("No autenticado");
    }

    @Test
    void crear_userIdNull_forbidden() {
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 1L, null, null
        );

        when(currentUser.userId(auth)).thenReturn(null);

        assertThatThrownBy(() -> service.crearMiEmprendimiento(auth, dto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("No autenticado");
    }

    @Test
    void crear_siYaExiste_emprendimiento_conflict() {
        Long userId = 10L;
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 1L, null, null
        );

        when(currentUser.userId(auth)).thenReturn(userId);
        when(emprendimientoRepository.existsByUsuarioId(userId)).thenReturn(true);

        assertThatThrownBy(() -> service.crearMiEmprendimiento(auth, dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya tiene un emprendimiento");

        verify(emprendimientoRepository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any(), any());
    }

    @Test
    void crear_usuarioNoExiste_EntityNotFound() {
        Long userId = 10L;
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 1L, null, null
        );

        when(currentUser.userId(auth)).thenReturn(userId);
        when(emprendimientoRepository.existsByUsuarioId(userId)).thenReturn(false);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearMiEmprendimiento(auth, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(emprendimientoRepository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any(), any());
    }

    @Test
    void crear_localidadNoExiste_EntityNotFound() {
        Long userId = 10L;
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 99L, null, null
        );

        when(currentUser.userId(auth)).thenReturn(userId);
        when(emprendimientoRepository.existsByUsuarioId(userId)).thenReturn(false);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(new Usuario()));
        when(localidadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearMiEmprendimiento(auth, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Localidad no encontrada");

        verify(emprendimientoRepository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any(), any());
    }

    @Test
    void crear_localidadInactiva_badRequest() {
        Long userId = 10L;
        var dto = new DatosCrearEmprendimiento(
                "Nombre", null, null, null, null,
                null, 1L, null, null
        );

        Usuario u = new Usuario();
        Localidad l = new Localidad();
        l.setActivo(false);

        when(currentUser.userId(auth)).thenReturn(userId);
        when(emprendimientoRepository.existsByUsuarioId(userId)).thenReturn(false);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(l));

        assertThatThrownBy(() -> service.crearMiEmprendimiento(auth, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactiva");

        verify(emprendimientoRepository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any(), any());
    }

    @Test
    void crear_ok_guarda_y_mapeaDetalle() {
        Long userId = 10L;
        var dto = new DatosCrearEmprendimiento(
                "Panadería La Esquina",
                "desc",
                "341",
                "insta",
                "face",
                "", // blank
                1L,
                "San Martín 123",
                "2000"
        );

        Usuario u = new Usuario();
        u.setId(userId);

        Localidad l = new Localidad();
        l.setId(1L);
        l.setNombre("Rosario");
        l.setActivo(true);

        when(currentUser.userId(auth)).thenReturn(userId);
        when(emprendimientoRepository.existsByUsuarioId(userId)).thenReturn(false);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(l));

        PerfilEmprendimiento peAConstruir = new PerfilEmprendimiento();
        peAConstruir.setUsuario(u);
        peAConstruir.setLocalidad(l);
        peAConstruir.setNombre(dto.nombre());

        when(mapper.toEntity(dto, u, l)).thenReturn(peAConstruir);

        PerfilEmprendimiento guardado = new PerfilEmprendimiento();
        guardado.setId(777L);
        guardado.setUsuario(u);
        guardado.setLocalidad(l);
        guardado.setNombre(dto.nombre());
        guardado.setActivo(true);

        when(emprendimientoRepository.save(any())).thenReturn(guardado);
        when(mapper.toDetalle(guardado)).thenReturn(null);

        service.crearMiEmprendimiento(auth, dto);

        ArgumentCaptor<PerfilEmprendimiento> captor = ArgumentCaptor.forClass(PerfilEmprendimiento.class);
        verify(emprendimientoRepository).save(captor.capture());

        assertThat(captor.getValue()).isSameAs(peAConstruir);

        verify(mapper).toEntity(dto, u, l);
        verify(mapper).toDetalle(guardado);
    }
}

