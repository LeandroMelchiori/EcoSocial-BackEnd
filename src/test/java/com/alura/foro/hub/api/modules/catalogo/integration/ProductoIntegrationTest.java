package com.alura.foro.hub.api.modules.catalogo.integration;

import com.alura.foro.hub.api.modules.catalogo.domain.CategoriaCatalogo;
import com.alura.foro.hub.api.modules.catalogo.domain.Subcategoria;
import com.alura.foro.hub.api.modules.catalogo.repository.CategoriaCatalogoRepository;
import com.alura.foro.hub.api.modules.catalogo.repository.SubCategoriaCatalogoRepository;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // <-- CLAVE: habilita transacción para el @BeforeEach
class ProductoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaCatalogoRepository categoriaRepo;
    @Autowired SubCategoriaCatalogoRepository subcategoriaRepo;

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
    void setup() {
        // ----- USUARIO (sin Perfil, tu getAuthorities() devuelve ROLE_USER igual) -----
        usuario = new Usuario();
        usuario.setNombre("Sacha Test");
        usuario.setEmail("sacha.test@mail.com");
        usuario.setUsername("sacha_test");
        usuario.setPassword("123456");
        usuario = usuarioRepository.save(usuario);

        // ----- CATEGORIA -----
        categoria = new CategoriaCatalogo();
        categoria.setNombre("Tecnologia");
        categoria.setActivo(true);
        categoria = categoriaRepo.save(categoria);

        // ----- SUBCATEGORIA -----
        subcategoria = new Subcategoria();
        subcategoria.setCategoria(categoria);
        subcategoria.setNombre("Celulares");
        subcategoria.setActivo(true);
        subcategoria = subcategoriaRepo.save(subcategoria);
    }

    @Test
    void flujo_producto_crear_detalle_listar_eliminar_ok() throws Exception {

        // principal = Usuario real, como espera tu controller
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

        // Imagen fake PNG (tu validador revisa contentType)
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
                .andExpect(jsonPath("$.id").value((int) productoId))
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

        // OJO: tu delete mueve a trash y purga AFTER COMMIT.
        // Como este test está dentro de transacción (rollback al final), el commit no ocurre.
        // Entonces NO podés afirmar que se borró el storage final acá.
        // En vez de eso, verificamos que el recurso ya no existe en DB vía GET.

        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isNotFound());
    }
}
