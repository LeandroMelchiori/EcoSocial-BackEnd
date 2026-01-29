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

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoIntegrationCommitTest {

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
        usuario = new Usuario();
        usuario.setNombre("Sacha");
        usuario.setApellido("Test");
        usuario.setDni("12345678"); // o uno random, pero que sea numérico y único si corrés varios tests
        usuario.setEmail("sacha.commit@mail.com");
        usuario.setPassword("123456");
        usuario = usuarioRepository.save(usuario);

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
    void delete_commit_purge_storage_ok() throws Exception {

        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        // -------- CREATE --------
        String jsonData = """
                {
                  "categoriaCatalogoId": %d,
                  "subCategoriaCatalogoId": %d,
                  "titulo": "Moto G Power",
                  "descripcion": "Telefono de prueba para validar commit y purge de storage correctamente."
                }
                """.formatted(categoria.getId(), subcategoria.getId());

        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonData.getBytes(StandardCharsets.UTF_8)
        );

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
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode created = mapper.readTree(createRes.getResponse().getContentAsString());
        long productoId = created.get("id").asLong();

        Path finalDir = uploadsRoot.resolve(Path.of("productos", String.valueOf(productoId)));
        assertThat(Files.exists(finalDir)).isTrue();

        // -------- DELETE (commit real) --------
        mvc.perform(delete("/catalogo/productos/{id}", productoId)
                        .with(authentication(auth)))
                .andExpect(status().isNoContent());

        // 1) DB: ya no existe
        mvc.perform(get("/catalogo/productos/{id}", productoId))
                .andExpect(status().isNotFound());

        // 2) Storage: carpeta final ya no debería existir
        assertThat(Files.exists(finalDir)).isFalse();

        // 3) Storage: trash no debería contener ARCHIVOS (pueden quedar carpetas vacías)
        Path trashRoot = uploadsRoot.resolve("trash");
        if (Files.exists(trashRoot)) {
            try (Stream<Path> s = Files.walk(trashRoot)) {
                long filesCount = s
                        .filter(p -> !p.equals(trashRoot))
                        .filter(Files::isRegularFile)   // <- clave: solo archivos
                        .count();

                assertThat(filesCount).isZero();
            }
        }

    }
}
