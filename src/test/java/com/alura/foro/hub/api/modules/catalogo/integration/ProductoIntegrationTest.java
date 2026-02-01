package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import com.alura.foro.hub.api.modules.catalogo.repository.CategoriaCatalogoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.SubCategoriaCatalogoRepository;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaCatalogoRepository categoriaRepo;
    @Autowired SubCategoriaCatalogoRepository subcategoriaRepo;

    @Autowired LocalidadRepository localidadRepository;
    @Autowired PerfilEmprendimientoRepository perfilEmprendimientoRepository;

    @TempDir
    static Path tempDir;

    private static Path uploadsRoot;

    private Usuario usuario;
    private CategoriaCatalogo categoria;
    private Subcategoria subcategoria;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        uploadsRoot = tempDir.resolve("uploads-test");
        r.add("catalogo.storage", () -> "local");
        r.add("catalogo.local.root", () -> uploadsRoot.toString());
    }

    @BeforeEach
    void setup() throws Exception {

        // Limpieza por si quedó algo (no debería por @TempDir, pero mejor)
        if (Files.exists(uploadsRoot)) {
            try (var s = Files.walk(uploadsRoot)) {
                s.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }

        // -------------------------
        // 1) Usuario
        // -------------------------
        usuario = new Usuario();
        usuario.setNombre("User");
        usuario.setApellido("Test");
        usuario.setDni(UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 8));
        usuario.setEmail("user_test_" + UUID.randomUUID() + "@mail.com");
        usuario.setPassword("123456");
        usuario = usuarioRepository.save(usuario);

        // -------------------------
        // 2) Localidad (obligatoria para PerfilEmprendimiento)
        // -------------------------
        Localidad loc = new Localidad();
        loc.setGeorefId("test-" + UUID.randomUUID()); // not null + unique
        loc.setNombre("Rosario");                     // not null
        loc.setDepartamento("Rosario");
        loc.setActivo(true);
        loc = localidadRepository.save(loc);

        // -------------------------
        // 3) PerfilEmprendimiento (requisito para publicar productos)
        // -------------------------
        PerfilEmprendimiento emp = new PerfilEmprendimiento();
        emp.setUsuario(usuario);
        emp.setNombre("Emprendimiento Test");
        emp.setDescripcion("Creado para tests de integración");
        emp.setLocalidad(loc);
        emp.setActivo(true);
        // provincia default "Santa Fe" en tu entidad
        perfilEmprendimientoRepository.save(emp);

        // -------------------------
        // 4) Categoria / Subcategoria
        // -------------------------
        categoria = new CategoriaCatalogo();
        categoria.setNombre("Tecnologia");
        categoria.setActivo(true);
        categoria = categoriaRepo.save(categoria);

        subcategoria = new Subcategoria();
        subcategoria.setCategoria(categoria);
        subcategoria.setNombre("Celulares");
        subcategoria.setActivo(true);
        subcategoria = subcategoriaRepo.save(subcategoria);
    }

    @Test
    void flujo_producto_crear_detalle_listar_eliminar_ok() throws Exception {

        // principal = Usuario real
        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        // -------- POST multipart --------
        String jsonData = """
                {
                  "categoriaCatalogoId": %d,
                  "subCategoriaCatalogoId": %d,
                  "titulo": "Redmi Note 14 Pro",
                  "descripcion": "Telefono con excelente bateria, camara y rendimiento para el precio."
                }
                """.formatted(categoria.getId(), subcategoria.getId());

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // Imagen fake PNG (header válido)
        byte[] fakePng = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile img1 = new MockMultipartFile(
                "imagenes",
                "img1.png",
                "image/png",
                fakePng
        );

        MvcResult createRes = mvc.perform(
                        multipart("/catalogo/productos")
                                .file(dataPart)
                                .file(img1)
                                .with(authentication(auth))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/catalogo/productos/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Redmi Note 14 Pro"))
                .andExpect(jsonPath("$.imagenes").isArray())
                .andReturn();

        JsonNode created = mapper.readTree(createRes.getResponse().getContentAsString());
        long productoId = created.get("id").asLong();

        // Validación storage: carpeta productos/{id} debería existir
        Path dirProducto = uploadsRoot.resolve(Path.of("productos", String.valueOf(productoId)));
        assertThat(Files.exists(dirProducto)).isTrue();

        // -------- GET detalle --------
        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId))
                .andExpect(jsonPath("$.titulo").value("Redmi Note 14 Pro"))
                .andExpect(jsonPath("$.imagenes").isArray());

        // -------- GET listado --------
        mvc.perform(get("/catalogo/productos")
                        .param("q", "redmi")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // -------- DELETE --------
        mvc.perform(delete("/catalogo/productos/{id}", productoId)
                        .with(authentication(auth))
                )
                .andExpect(status().isNoContent());

        // verificamos que el recurso ya no existe en DB vía GET.
        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isNotFound());
    }
}
