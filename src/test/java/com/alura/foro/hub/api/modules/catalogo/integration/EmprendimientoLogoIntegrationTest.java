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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmprendimientoLogoIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    LocalidadRepository localidadRepository;

    @Autowired
    PerfilEmprendimientoRepository emprendimientoRepository;

    @MockitoBean
    StorageService storageService;

    private Usuario usuario;
    private PerfilEmprendimiento emp;

    @BeforeEach
    void setup() {
        emprendimientoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localidadRepository.deleteAll();

        // Localidad
        Localidad loc = new Localidad();
        loc.setGeorefId("geo-1");
        loc.setNombre("Rosario");
        loc.setDepartamento("Santa Fe");
        loc.setLat(-32.9442);
        loc.setLon(-60.6505);
        loc.setActivo(true);
        loc = localidadRepository.save(loc);

        // Usuario
        usuario = new Usuario();
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setDni("99999999");
        usuario.setEmail("test@foro.com");
        usuario.setPassword("123"); // en test no importa
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);

        // Perfil Emprendimiento
        emp = new PerfilEmprendimiento();
        emp.setUsuario(usuario);
        emp.setNombre("Mi emp");
        emp.setLocalidad(loc);
        emp.setActivo(true);
        emp = emprendimientoRepository.save(emp);
    }

    @Test
    @WithMockUser(username = "test@foro.com")
    void subirLogo_ok() throws Exception {

        // mock del storage
        when(storageService.saveEmprendimientoLogo(eq(emp.getId()), any()))
                .thenReturn("emprendimientos/" + emp.getId() + "/logo.png");

        MockMultipartFile logo = new MockMultipartFile(
                "logo",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mvc.perform(
                        multipart("/me/emprendimiento/logo")
                                .file(logo)
                                .with(req -> { req.setMethod("PUT"); return req; })
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(emp.getId()))
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.logoKey").exists());

        // VERIFY: verificar persistencia real
        PerfilEmprendimiento empDb =
                emprendimientoRepository.findById(emp.getId()).orElseThrow();

        assertThat(empDb.getLogoKey())
                .isEqualTo("emprendimientos/" + emp.getId() + "/logo.png");

        // VERIFY: verificar interacción con storage
        verify(storageService)
                .saveEmprendimientoLogo(eq(emp.getId()), any());
    }
}
