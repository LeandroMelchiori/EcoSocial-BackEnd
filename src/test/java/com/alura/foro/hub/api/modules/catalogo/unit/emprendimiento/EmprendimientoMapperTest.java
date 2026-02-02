package com.alura.foro.hub.api.modules.catalogo.unit.emprendimiento;

import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.mapper.EmprendimientoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprendimientoMapperTest {

    @Mock
    StorageService storage;

    @InjectMocks
    EmprendimientoMapper mapper;

    @Test
    void toDetalle_sinLogoKey_logoUrlNull() {
        var u = new Usuario();
        u.setId(10L);

        var loc = new Localidad();
        loc.setId(1L);
        loc.setNombre("Rosario");

        var pe = new PerfilEmprendimiento();
        pe.setId(5L);
        pe.setUsuario(u);
        pe.setNombre("X");
        pe.setLogoKey("   "); // blank
        pe.setProvincia("Santa Fe");
        pe.setLocalidad(loc);
        pe.setActivo(true);

        var dto = mapper.toDetalle(pe);

        assertNull(dto.logoUrl());
        verify(storage, never()).getUrl(anyString());
    }

    @Test
    void toDetalle_conLogoKey_poneUrl() {
        when(storage.getUrl("logos/1.png")).thenReturn("http://url");

        var u = new Usuario();
        u.setId(10L);

        var loc = new Localidad();
        loc.setId(1L);
        loc.setNombre("Rosario");

        var pe = new PerfilEmprendimiento();
        pe.setId(5L);
        pe.setUsuario(u);
        pe.setNombre("X");
        pe.setProvincia("Santa Fe");
        pe.setLocalidad(loc);
        pe.setActivo(true);
        pe.setLogoKey("logos/1.png");

        var dto = mapper.toDetalle(pe);

        assertEquals("http://url", dto.logoUrl());
        verify(storage).getUrl("logos/1.png");
    }
}
