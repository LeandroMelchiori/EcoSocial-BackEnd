package com.alura.foro.hub.api;

import com.alura.foro.hub.api.domain.Usuario;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private RestTemplate restTemplate;
    private String baseUrl;
    private String token;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        baseUrl = "http://localhost:" + port;

        Usuario usuario = new Usuario();
        usuario.setUsername("usuario_test");
        usuario.setEmail("test@example.com");
        usuario.setPassword(new BCryptPasswordEncoder().encode("password"));
        usuarioRepository.save(usuario);

        // Obtener el token
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/auth/login?username=usuario_test&password=password", null, String.class
        );
        token = response.getBody();
    }

    @Test
    void testAccesoConToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/usuarios", HttpMethod.GET, entity, String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAccesoSinToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/usuarios", String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
