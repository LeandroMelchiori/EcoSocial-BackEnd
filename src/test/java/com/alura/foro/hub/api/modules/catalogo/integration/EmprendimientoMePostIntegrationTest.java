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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EmprendimientoMePostIntegrationTest {

    @Autowired MockMvc mvc;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired LocalidadRepository localidadRepository;
    @Autowired PerfilEmprendimientoRepository emprendimientoRepository;

    private Usuario user;
    private Localidad locActiva;

    @BeforeEach
    void setup() {
        user = usuarioRepository.findByEmail("user@test.com").orElseGet(() -> {
            var u = new Usuario();
            u.setEmail("user@test.com");
            u.setDni("30111222"); // asegurate que no choque
            u.setNombre("User");
            u.setApellido("Apellido");
            u.setPassword("123");
            return usuarioRepository.saveAndFlush(u);
        });

        locActiva = localidadRepository.findAll().stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()))
                .findFirst()
                .orElseGet(this::crearLocalidadActivaSiNoHay);

        emprendimientoRepository.findByUsuarioId(user.getId())
                .ifPresent(emprendimientoRepository::delete);
        emprendimientoRepository.flush();
    }

    private Localidad crearLocalidadActivaSiNoHay() {
        var l = new Localidad();
        l.setNombre("Rosario");
        l.setActivo(true);

        l.setGeorefId("TEST-" + UUID.randomUUID());
        l.setDepartamento("Rosario");
        l.setLat(-32.9587);
        l.setLon(-60.6939);

        return localidadRepository.saveAndFlush(l);
    }

    private String bodyOk(Long localidadId, boolean incluirProvincia, String provinciaValue, String nombre) {
        String provinciaJson = incluirProvincia
                ? """
                  ,"provincia": %s
                  """.formatted(provinciaValue == null ? "null" : "\"" + provinciaValue + "\"")
                : "";

        return ("""
        {
          "nombre": "%s",
          "descripcion": "Pan casero y facturas",
          "telefonoContacto": "3411234567",
          "instagram": "laesquina.pan",
          "facebook": "La Esquina Pan"%s,
          "localidadId": %d,
          "direccion": "San Martín 123",
          "codigoPostal": "2000"
        }
        """).formatted(nombre, provinciaJson, localidadId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_ok_201() throws Exception {
        String body = bodyOk(locActiva.getId(), true, "", "Panadería La Esquina");

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/me/emprendimiento")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.usuarioId").value(user.getId()))
                .andExpect(jsonPath("$.nombre").value("Panadería La Esquina"))
                .andExpect(jsonPath("$.provincia").value("Santa Fe"))
                .andExpect(jsonPath("$.localidadId").value(locActiva.getId()))
                .andExpect(jsonPath("$.localidadNombre").value(locActiva.getNombre()))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crear_sinAuth_401_o_403() throws Exception {
        String body = bodyOk(locActiva.getId(), true, "", "Panadería La Esquina");

        MvcResult result = mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_nombreVacio_400() throws Exception {
        String body = bodyOk(locActiva.getId(), true, "", "");

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_sinLocalidadId_400() throws Exception {
        String body = """
        {
          "nombre": "Panadería La Esquina",
          "descripcion": "Pan casero"
        }
        """;

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_localidadNoExiste_404() throws Exception {
        long inexistente = 999999L;
        String body = bodyOk(inexistente, true, "", "Panadería La Esquina");

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_localidadInactiva_400() throws Exception {
        Localidad inactiva = new Localidad();
        inactiva.setNombre("Test Inactiva");
        inactiva.setActivo(false);
        inactiva.setGeorefId("TEST-INACT-" + UUID.randomUUID());
        inactiva.setDepartamento("Test");
        inactiva.setLat(0.0);
        inactiva.setLon(0.0);
        inactiva = localidadRepository.saveAndFlush(inactiva);

        String body = bodyOk(inactiva.getId(), true, "", "Panadería La Esquina");

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("inactiva")));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_usuarioYaTieneEmprendimiento_400_o_409() throws Exception {
        var pe = new PerfilEmprendimiento();
        pe.setUsuario(user);
        pe.setNombre("Existente");
        pe.setLocalidad(locActiva);
        emprendimientoRepository.saveAndFlush(pe);

        String body = bodyOk(locActiva.getId(), true, "", "Nuevo");

        MvcResult result = mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(400, 409);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void crear_sinProvincia_defaultSantaFe() throws Exception {
        String body = bodyOk(locActiva.getId(), false, null, "Panadería Sin Provincia");

        mvc.perform(post("/me/emprendimiento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provincia").value("Santa Fe"));
    }
}
