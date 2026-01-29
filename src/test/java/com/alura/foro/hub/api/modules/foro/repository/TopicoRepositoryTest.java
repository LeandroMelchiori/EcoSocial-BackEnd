package com.alura.foro.hub.api.modules.foro.repository;

import com.alura.foro.hub.api.modules.foro.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.modules.foro.domain.model.Categoria;
import com.alura.foro.hub.api.modules.foro.domain.model.Curso;
import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.user.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = com.alura.foro.hub.api.Application.class)
@Import(TopicoRepositoryTest.AuditingTestConfig.class)
class TopicoRepositoryTest {

    @Autowired
    TopicoRepository topicoRepository;
    @Autowired TestEntityManager em;

    private Usuario autor;
    private Usuario autor2;
    private Curso curso;
    private Curso curso2;

    @BeforeEach
    void setup() {
        autor = persistUsuario("autor1", "Autor Uno");
        autor2 = persistUsuario("autor2", "Autor Dos");

        Categoria categoria = persistCategoria();
        curso = persistCurso("Java", categoria);
        curso2 = persistCurso("Spring", categoria);
    }

    @Test
    void save_y_findById() {
        var topico = persistTopico("Titulo 1", "Mensaje 1", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now().minusDays(1));

        var encontrado = topicoRepository.findById(topico.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("Titulo 1");
    }

    @Test
    void findByCurso() {
        var t1 = persistTopico("A", "M1", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        var t2 = persistTopico("B", "M2", autor, curso2, StatusTopico.ACTIVO, LocalDateTime.now());

        var res = topicoRepository.findByCurso(curso);

        assertThat(res).extracting(Topico::getId).contains(t1.getId());
        assertThat(res).extracting(Topico::getId).doesNotContain(t2.getId());
    }

    @Test
    void findByAutorEmail() {
        var t1 = persistTopico("A", "M1", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        persistTopico("B", "M2", autor2, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        var res = topicoRepository.findByAutorEmail("autor1@test.com");

        assertThat(res).extracting(Topico::getId).contains(t1.getId());
        assertThat(res).allMatch(t -> t.getAutor().getEmail().equals("autor1@test.com"));
    }

    @Test
    void findByStatus() {
        var t1 = persistTopico("A", "M1", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        persistTopico("B", "M2", autor, curso, StatusTopico.CERRADO, LocalDateTime.now());

        var res = topicoRepository.findByStatus(StatusTopico.ACTIVO);

        assertThat(res).extracting(Topico::getId).contains(t1.getId());
        assertThat(res).allMatch(t -> t.getStatus() == StatusTopico.ACTIVO);
    }

    @Test
    void findByTituloContainingIgnoreCase() {
        var t1 = persistTopico("Spring Security", "M1", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        persistTopico("Hibernate Tips", "M2", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        var res = topicoRepository.findByTituloContainingIgnoreCase("sPriNg");

        assertThat(res).extracting(Topico::getId).contains(t1.getId());
        assertThat(res).hasSize(1);
    }

    @Test
    void listarConMetricas_devuelve_count_y_maxFechaRespuesta() {
        var topico = persistTopico("Metricas", "Mensaje", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now().minusDays(2));

        // 2 respuestas con fechas distintas
        persistRespuesta(topico, autor2, "R1", LocalDateTime.now().minusDays(1));
        persistRespuesta(topico, autor2, "R2", LocalDateTime.now());

        var page = topicoRepository.listarConMetricas(PageRequest.of(0, 10));

        var dto = page.getContent().stream()
                .filter(d -> d.id().equals(topico.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(dto.cantidadRespuestas()).isEqualTo(2);
        assertThat(dto.fechaUltimaRespuesta()).isNotNull();
    }

    @Test
    void buscarConMetricas_filtra_por_texto_status_y_rango_fechas() {
        var viejo = persistTopico("Viejo", "Texto viejo", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now().minusDays(10));
        var match = persistTopico("Java Streams", "Hola mundo streams", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now().minusDays(2));
        persistTopico("Otro", "Nada que ver", autor, curso, StatusTopico.CERRADO, LocalDateTime.now().minusDays(2));

        var desde = LocalDateTime.now().minusDays(5);
        var hasta = LocalDateTime.now().minusDays(1);

        var page = topicoRepository.buscarConMetricas(
                "streams",
                curso.getId(),
                autor.getId(),
                StatusTopico.ACTIVO,
                desde,
                hasta,
                "Java",
                "Programación",
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).extracting(DatosListadoTopico::id).contains(match.getId());
        assertThat(page.getContent()).extracting(DatosListadoTopico::id).doesNotContain(viejo.getId());
    }

    @Test
    void buscarDetallePorId_trae_autor_curso_categoria_y_respuestas_con_autor() {
        var topico = persistTopico("Detalle", "Mensaje", autor, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        persistRespuesta(topico, autor2, "Respuesta 1", LocalDateTime.now());

        em.flush();
        em.clear(); // clave para asegurarnos que realmente haga fetch join

        var opt = topicoRepository.buscarDetallePorId(topico.getId());
        assertThat(opt).isPresent();

        var t = opt.get();

        // Si esto explota con LazyInitialization, tu fetch join no está funcionando
        assertThat(t.getAutor().getNombre()).isNotBlank();
        assertThat(t.getCurso().getNombre()).isNotBlank();
        assertThat(t.getCurso().getCategoria().getNombre()).isNotBlank();
        assertThat(t.getRespuestas()).hasSize(1);

        // ✅ NO username: el identificador real es email/dni
        assertThat(t.getRespuestas().get(0).getAutor().getEmail()).isEqualTo(emailDesde("autor2"));
    }

    // ------------------ helpers ------------------

    private Usuario persistUsuario(String login, String nombre) {
        var u = new Usuario();
        u.setNombre(nombre);
        u.setApellido("Test");
        u.setDni(dniDesde(login));
        u.setEmail(emailDesde(login));
        u.setPassword("123");
        return em.persistAndFlush(u);
    }

    private Categoria persistCategoria() {
        var c = new Categoria();
        c.setNombre("Programación");
        return em.persistAndFlush(c);
    }

    private Curso persistCurso(String nombre, Categoria categoria) {
        var c = new Curso();
        c.setNombre(nombre);
        c.setCategoria(categoria);
        return em.persistAndFlush(c);
    }

    private Topico persistTopico(String titulo, String mensaje, Usuario autor, Curso curso, StatusTopico status, LocalDateTime fecha) {
        var t = new Topico();
        t.setTitulo(titulo);
        t.setMensaje(mensaje);
        t.setAutor(autor);
        t.setCurso(curso);
        t.setStatus(status);
        t.setFechaCreacion(fecha);
        return em.persistAndFlush(t);
    }

    private void persistRespuesta(Topico topico, Usuario autor, String mensaje, LocalDateTime fecha) {
        var r = new Respuesta();
        r.setTopico(topico);
        r.setAutor(autor);
        r.setMensaje(mensaje);
        r.setFechaCreacion(fecha);
        em.persistAndFlush(r);
    }

    @EnableJpaAuditing
    static class AuditingTestConfig {
        @Bean
        AuditorAware<Long> auditorProvider() {
            return () -> Optional.of(1L);
        }
    }

    private String emailDesde(String login) {
        if (login == null || login.isBlank()) return "user@test.com";
        return login.contains("@") ? login : login + "@test.com";
    }

    private String dniDesde(String login) {
        if (login != null && login.matches("\\d{7,20}")) return login;
        int base = Math.abs((login == null ? "user" : login).hashCode());
        return String.format("%08d", base % 100_000_000);
    }
}

