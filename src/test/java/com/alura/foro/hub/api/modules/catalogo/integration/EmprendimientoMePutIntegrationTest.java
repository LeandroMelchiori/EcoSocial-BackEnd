package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmprendimientoMePutIntegrationTest {

    @Autowired MockMvc mvc;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired LocalidadRepository localidadRepository;
    @Autowired PerfilEmprendimientoRepository emprendimientoRepository;

    @MockitoBean
    StorageService storageService;

    private Usuario usuario;
    private Localidad rosario;
    private Localidad funes;
    private PerfilEmprendimiento emp;

    @BeforeEach
    void setup() {
        emprendimientoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localidadRepository.deleteAll();

        rosario = new Localidad();
        rosario.setGeorefId("geo-1");
        rosario.setNombre("Rosario");
        rosario.setDepartamento("Santa Fe");
        rosario.setLat(-32.9442);
        rosario.setLon(-60.6505);
        rosario.setActivo(true);
        rosario = localidadRepository.save(rosario);

        funes = new Localidad();
        funes.setGeorefId("geo-2");
        funes.setNombre("Funes");
        funes.setDepartamento("Santa Fe");
        funes.setLat(-32.9167);
        funes.setLon(-60.8167);
        funes.setActivo(true);
        funes = localidadRepository.save(funes);

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
        emp.setLocalidad(rosario);
        emp.setActivo(true);
        // logoKey queda null (como en tu response real del GET)
        emp = emprendimientoRepository.save(emp);

        // Por si algún día logoKey deja de ser null, no queremos que el test explote
        when(storageService.getUrl(anyString())).thenReturn("http://fake-url");
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void actualizarMiEmprendimiento_ok() throws Exception {

        String body = """
                {
                  "nombre": "Mi emp actualizado",
                  "descripcion": "Nueva descripcion",
                  "telefonoContacto": "341-999",
                  "instagram": "@nuevo",
                  "facebook": "fb nuevo",
                  "localidadId": %d,
                  "direccion": "Nueva direccion 123",
                  "codigoPostal": "2001"
                }
                """.formatted(funes.getId());

        mvc.perform(
                        put("/me/emprendimiento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                // base
                .andExpect(jsonPath("$.id").value(emp.getId()))
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.activo").value(true))
                // campos actualizados
                .andExpect(jsonPath("$.nombre").value("Mi emp actualizado"))
                .andExpect(jsonPath("$.descripcion").value("Nueva descripcion"))
                .andExpect(jsonPath("$.telefonoContacto").value("341-999"))
                .andExpect(jsonPath("$.instagram").value("@nuevo"))
                .andExpect(jsonPath("$.facebook").value("fb nuevo"))
                .andExpect(jsonPath("$.direccion").value("Nueva direccion 123"))
                .andExpect(jsonPath("$.codigoPostal").value("2001"))
                // localidad
                .andExpect(jsonPath("$.localidadId").value(funes.getId()))
                .andExpect(jsonPath("$.localidadNombre").value("Funes"))
                // provincia queda por defecto (no se actualiza en este endpoint)
                .andExpect(jsonPath("$.provincia").value("Santa Fe"))
                // logo en null (mapper devuelve null logoUrl si logoKey es null)
                .andExpect(jsonPath("$.logoKey").isEmpty())
                .andExpect(jsonPath("$.logoUrl").isEmpty());

        // VERIFY: Verificación real en DB
        PerfilEmprendimiento empDb = emprendimientoRepository.findById(emp.getId()).orElseThrow();
        assertThat(empDb.getNombre()).isEqualTo("Mi emp actualizado");
        assertThat(empDb.getDescripcion()).isEqualTo("Nueva descripcion");
        assertThat(empDb.getTelefonoContacto()).isEqualTo("341-999");
        assertThat(empDb.getInstagram()).isEqualTo("@nuevo");
        assertThat(empDb.getFacebook()).isEqualTo("fb nuevo");
        assertThat(empDb.getDireccion()).isEqualTo("Nueva direccion 123");
        assertThat(empDb.getCodigoPostal()).isEqualTo("2001");
        assertThat(empDb.getLocalidad().getId()).isEqualTo(funes.getId());
        assertThat(empDb.getProvincia()).isEqualTo("Santa Fe");
        assertThat(empDb.getFechaActualizacion()).isNotNull();
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void actualizarMiEmprendimiento_nombreBlank_400() throws Exception {
        String body = """
                {
                  "nombre": "   ",
                  "localidadId": %d
                }
                """.formatted(rosario.getId());

        mvc.perform(
                        put("/me/emprendimiento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void actualizarMiEmprendimiento_sinPerfil_404() throws Exception {
        emprendimientoRepository.deleteAll();

        String body = """
                {
                  "nombre": "Mi emp actualizado",
                  "localidadId": %d
                }
                """.formatted(rosario.getId());

        mvc.perform(
                        put("/me/emprendimiento")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }
}
