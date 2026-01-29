package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import com.alura.foro.hub.api.modules.catalogo.repository.CategoriaCatalogoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.SubCategoriaCatalogoRepository;
import com.alura.foro.hub.api.security.jwt.TokenService;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoIntegrationUpdateCommitTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaCatalogoRepository categoriaRepository;
    @Autowired SubCategoriaCatalogoRepository subcategoriaRepository;

    @Autowired TokenService tokenService;
    @Autowired PasswordEncoder passwordEncoder;

    @Value("${catalogo.local.root:uploads}")
    String uploadsRoot;

    private Path uploadsPath;

    private String authHeader;

    private CategoriaCatalogo categoria;
    private Subcategoria subcategoria;

    @BeforeEach
    void setup() throws Exception {
        uploadsPath = Paths.get(uploadsRoot).toAbsolutePath().normalize();
        purgeDir(uploadsPath);

        // ---------- Usuario ----------
        Usuario usuario = new Usuario();
        usuario.setNombre("User");
        usuario.setApellido("Test");
        usuario.setDni(generarDni8()); // helper abajo

        String email = "user_test_" + UUID.randomUUID() + "@mail.com";
        usuario.setEmail(email);

        usuario.setPassword(passwordEncoder.encode("123456"));
        usuario = usuarioRepository.save(usuario);

// JWT REAL (lo que tu SecurityFilter espera)
        authHeader = "Bearer " + tokenService.generateToken(usuario);


        // ---------- Categoria ----------
        categoria = new CategoriaCatalogo();
        categoria.setNombre("Tecnología");
        categoria = categoriaRepository.save(categoria);

        // ---------- Subcategoria ----------
        subcategoria = new Subcategoria();
        subcategoria.setNombre("Celulares");

        // OJO: ajustá según tu entidad:
        // - si es setCategoriaCatalogo(categoria) o setCategoria(categoria)
        subcategoria.setCategoria(categoria);

        subcategoria = subcategoriaRepository.save(subcategoria);
    }

    @AfterEach
    void teardown() throws Exception {
        purgeDir(uploadsPath);
    }

    @Test
    @Order(1)
    void update_commit_debe_borrar_viejas_y_purgar_temp_ok() throws Exception {
        // 1) CREAR producto con 2 imágenes
        Long productoId = crearProductoConImagenes(2);

        Path productoDir = uploadsPath.resolve(Paths.get("productos", String.valueOf(productoId)));
        assertThat(Files.exists(productoDir)).isTrue();

        List<Path> imagenesIniciales = listarArchivos(productoDir);
        assertThat(imagenesIniciales).hasSize(2);

        // 2) UPDATE con 1 imagen nueva (debe borrar las 2 viejas y dejar 1 nueva)
        String updateJson = """
                {
                  "categoriaCatalogoId": %d,
                  "subCategoriaCatalogoId": %d,
                  "titulo": "Moto G Power - Actualizado",
                  "descripcion": "Update para test de commit/purge",
                  "activo": true
                }
                """.formatted(categoria.getId(), subcategoria.getId());

        MockMultipartFile data = new MockMultipartFile(
                "data", "data.json", "application/json",
                updateJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile nuevaImg = new MockMultipartFile(
                "imagenes", "new.png", "image/png",
                fakePngBytes("NEW_IMAGE")
        );

        var updateResult = mvc.perform(
                        multipart("/catalogo/productos/{id}", productoId)
                                .file(data)
                                .file(nuevaImg)
                                // multipart() por defecto es POST. Forzamos PUT:
                                .with(req -> { req.setMethod("PUT"); return req; })
                                // ✅ AUTH REAL
                                .header(HttpHeaders.AUTHORIZATION, authHeader)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode updated = objectMapper.readTree(updateResult.getResponse().getContentAsString());

        // 3) Assert DB: producto existe y tiene 1 imagen
        assertThat(updated.get("id").asLong()).isEqualTo(productoId);
        assertThat(updated.get("imagenes").isArray()).isTrue();
        assertThat(updated.get("imagenes").size()).isEqualTo(1);

        // 4) Assert FS: se borraron las viejas y quedó 1 nueva
        List<Path> imagenesFinales = listarArchivos(productoDir);
        assertThat(imagenesFinales).hasSize(1);

        for (Path vieja : imagenesIniciales) {
            assertThat(Files.exists(vieja))
                    .as("La imagen vieja debería haberse borrado: " + vieja.getFileName())
                    .isFalse();
        }

        // 5) Assert NO quedó basura en temp (local)
        Path tempDir = uploadsPath.resolve("temp");
        if (Files.exists(tempDir)) {
            List<Path> tempFiles = Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            assertThat(tempFiles).isEmpty();
        }

        // 6) sanity check: GET detalle sigue funcionando
        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId))
                .andExpect(jsonPath("$.imagenes.length()").value(1));
    }

    // -------------------------
    // helpers
    // -------------------------

    private Long crearProductoConImagenes(int cantidadImgs) throws Exception {

        String crearJson = """
            {
              "categoriaCatalogoId": %d,
              "subCategoriaCatalogoId": %d,
              "titulo": "Moto G Power",
              "descripcion": "Producto inicial para test de update"
            }
            """.formatted(categoria.getId(), subcategoria.getId());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                "application/json",
                crearJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartHttpServletRequestBuilder builder =
                multipart("/catalogo/productos")
                        .file(data);

        for (int i = 1; i <= cantidadImgs; i++) {
            builder.file(new MockMultipartFile(
                    "imagenes",
                    "img" + i + ".png",
                    "image/png",
                    fakePngBytes("IMG_" + i)
            ));
        }

        builder
                // ✅ AUTH REAL
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        var result = mvc.perform(builder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        return created.get("id").asLong();
    }

    private static byte[] fakePngBytes(String seed) {
        return ("PNG_BYTES_" + seed + "_" + UUID.randomUUID())
                .getBytes(StandardCharsets.UTF_8);
    }

    private static List<Path> listarArchivos(Path dir) throws Exception {
        if (!Files.exists(dir)) return new ArrayList<>();
        try (var s = Files.list(dir)) {
            return s.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    private static void purgeDir(Path dir) throws Exception {
        if (dir == null || !Files.exists(dir)) return;
        Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // borrar hijos primero
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
    }

    private static String generarDni8() {
        // 8 dígitos numéricos
        int n = Math.abs(UUID.randomUUID().hashCode()) % 100_000_000;
        return String.format("%08d", n);
    }

}
