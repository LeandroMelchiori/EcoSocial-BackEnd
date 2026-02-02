package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmprendimientoMeGetIntegrationTest {

    @Autowired MockMvc mvc;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired LocalidadRepository localidadRepository;
    @Autowired PerfilEmprendimientoRepository emprendimientoRepository;

    private Usuario usuario;
    private Localidad loc;
    private PerfilEmprendimiento emp;

    @BeforeEach
    void setup() {
        emprendimientoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localidadRepository.deleteAll();

        loc = new Localidad();
        loc.setGeorefId("geo-1");
        loc.setNombre("Rosario");
        loc.setActivo(true);
        loc = localidadRepository.save(loc);

        usuario = new Usuario();
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setDni("99999999");
        usuario.setEmail("test@foro.com");
        usuario.setPassword("123");
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        emp = new PerfilEmprendimiento();
        emp.setUsuario(usuario);
        emp.setNombre("Mi emp");
        emp.setLocalidad(loc);
        emp.setActivo(true);
        emp.setLogoKey(null);
        emp = emprendimientoRepository.save(emp);
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void getMiEmprendimiento_ok() throws Exception {
        mvc.perform(get("/me/emprendimiento"))
                .andExpect(status().isOk())
                // Asumiendo DTO flat (como el del logo)
                .andExpect(jsonPath("$.id").value(emp.getId()))
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.nombre").value("Mi emp"))
                .andExpect(jsonPath("$.logoKey").isEmpty());// puede ser null y está bien
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void getMiEmprendimiento_sinPerfil_404() throws Exception {
        emprendimientoRepository.deleteAll(); // deja al usuario sin emprendimiento

        mvc.perform(get("/me/emprendimiento"))
                .andExpect(status().isNotFound());
    }
}
