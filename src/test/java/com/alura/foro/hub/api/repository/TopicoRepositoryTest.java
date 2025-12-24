package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TopicoRepositoryTest {

    @Autowired TopicoRepository topicoRepository;
    @Autowired EntityManager em;

    // ==============
    // Helpers
    // ==============

    private Usuario crearUsuario(String username, String nombre) {
        Usuario u = new Usuario();
        // TODO: ajustá a tus campos reales si tu entidad exige más cosas (email/password/etc.)
        u.setUsername(username);
        u.setNombre(nombre);
        em.persist(u);
        return u;
    }

    private Categoria crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        em.persist(c);
        return c;
    }

    private Curso crearCurso(String nombre, Categoria categoria) {
        Curso c = new Curso();
        c.setNombre(nombre);
        c.setCategoria(categoria);
        em.persist(c);
        return c;
    }

    private Topico crearTopico(String titulo, String mensaje, Usuario autor, Curso curso, StatusTopico status, LocalDateTime fechaCreacion) {
        Topico t = new Topico();
        t.setTitulo(titulo);
        t.setMensaje(mensaje);
        t.setAutor(autor);
        t.setCurso(curso);
        t.setStatus(status);
        t.setFechaCreacion(fechaCreacion);
        em.persist(t);
        return t;
    }

    private Respuesta crearRespuesta(Topico topico, Usuario autor, String mensaje, boolean solucion, LocalDateTime fechaCreacion) {
        Respuesta r = new Respuesta();
        r.setTopico(topico);
        r.setAutor(autor);
        r.setMensaje(mensaje);
        r.setSolucion(solucion);
        r.setFechaCreacion(fechaCreacion);
        em.persist(r);
        return r;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    // =========================
    // listarConMetricas
    // =========================
    @Test
    void listarConMetricas_calculaCantidadYUltimaRespuesta() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring Boot", cat);

        var autorTopico = crearUsuario("autor1", "Autor 1");
        var autorResp1 = crearUsuario("resp1", "Resp 1");
        var autorResp2 = crearUsuario("resp2", "Resp 2");

        var t1 = crearTopico(
                "Titulo 1", "Mensaje 1",
                autorTopico, curso,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 1, 10, 0)
        );

        // 2 respuestas con fechas distintas: la última debe ser la mayor
        crearRespuesta(t1, autorResp1, "R1", false, LocalDateTime.of(2025, 12, 2, 10, 0));
        crearRespuesta(t1, autorResp2, "R2", true,  LocalDateTime.of(2025, 12, 3, 10, 0));

        flushAndClear();

        // Act
        Page<DatosListadoTopico> page = topicoRepository.listarConMetricas(PageRequest.of(0, 10));

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        var dto = page.getContent().get(0);

        assertThat(dto.titulo()).isEqualTo("Titulo 1");
        assertThat(dto.nombreAutor()).isEqualTo("Autor 1");
        assertThat(dto.nombreCurso()).isEqualTo("Spring Boot");
        assertThat(dto.nombreCategoria()).isEqualTo("Backend");
        assertThat(dto.status()).isEqualTo(StatusTopico.ACTIVO);

        assertThat(dto.cantidadRespuestas()).isEqualTo(2L);
        assertThat(dto.fechaUltimaRespuesta()).isEqualTo(LocalDateTime.of(2025, 12, 3, 10, 0));
    }

    // =========================
    // buscarConMetricas - filtros
    // =========================
    @Test
    void buscarConMetricas_filtraPorNombreCurso_y_Categoria() {
        // Arrange
        var catBackend = crearCategoria("Backend");
        var catCloud = crearCategoria("Cloud");

        var cursoSpring = crearCurso("Spring Boot", catBackend);
        var cursoAws = crearCurso("AWS", catCloud);

        var autor1 = crearUsuario("autor1", "Autor 1");
        var autor2 = crearUsuario("autor2", "Autor 2");

        // Tópico que DEBE entrar (curso Spring y categoria Backend)
        var tOk = crearTopico(
                "JWT token", "Error con bearer",
                autor1, cursoSpring,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );
        crearRespuesta(tOk, autor2, "Te falta prefix", false, LocalDateTime.of(2025, 12, 11, 12, 0));

        // Ruido (otro curso / otra categoria)
        var tRuido = crearTopico(
                "S3", "Bucket policy",
                autor2, cursoAws,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );
        crearRespuesta(tRuido, autor1, "Probá IAM", false, LocalDateTime.of(2025, 12, 12, 12, 0));

        flushAndClear();

        // Act
        Page<DatosListadoTopico> page = topicoRepository.buscarConMetricas(
                null,             // q
                null,             // cursoId
                null,             // autorId
                null,             // status
                null,             // desde
                null,             // hasta
                "spring",         // nombreCurso (like %spring%)
                "back",           // nombreCategoria (like %back%)
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        var dto = page.getContent().get(0);
        assertThat(dto.titulo()).isEqualTo("JWT token");
        assertThat(dto.nombreCurso()).isEqualTo("Spring Boot");
        assertThat(dto.nombreCategoria()).isEqualTo("Backend");
        assertThat(dto.cantidadRespuestas()).isEqualTo(1L);
        assertThat(dto.fechaUltimaRespuesta()).isEqualTo(LocalDateTime.of(2025, 12, 11, 12, 0));
    }

    @Test
    void buscarConMetricas_filtraPorQ_enTitulo_o_Mensaje() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring Boot", cat);
        var autor = crearUsuario("autor", "Autor");

        crearTopico(
                "Error 403", "No autorizado",
                autor, curso,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 10, 10, 0)
        );

        crearTopico(
                "CORS", "Problema con securityFilter",
                autor, curso,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 10, 11, 0)
        );

        flushAndClear();

        // Act
        Page<DatosListadoTopico> page = topicoRepository.buscarConMetricas(
                "securityfilter", // q
                null, null, null,
                null, null,
                null, null,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).titulo()).isEqualTo("CORS");
    }

    @Test
    void buscarConMetricas_filtraPorStatus_y_RangoFechas() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring Boot", cat);
        var autor = crearUsuario("autor", "Autor");

        crearTopico(
                "T1", "m1",
                autor, curso,
                StatusTopico.ACTIVO,
                LocalDateTime.of(2025, 12, 1, 10, 0)
        );

        crearTopico(
                "T2", "m2",
                autor, curso,
                StatusTopico.CERRADO,
                LocalDateTime.of(2025, 12, 5, 10, 0)
        );

        crearTopico(
                "T3", "m3",
                autor, curso,
                StatusTopico.CERRADO,
                LocalDateTime.of(2025, 12, 20, 10, 0)
        );

        flushAndClear();

        // Act: CERRADO entre 2025-12-01 y 2025-12-10 => solo T2
        Page<DatosListadoTopico> page = topicoRepository.buscarConMetricas(
                null,
                null,
                null,
                StatusTopico.CERRADO,
                LocalDateTime.of(2025, 12, 1, 0, 0),
                LocalDateTime.of(2025, 12, 10, 23, 59),
                null,
                null,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).titulo()).isEqualTo("T2");
        assertThat(page.getContent().get(0).status()).isEqualTo(StatusTopico.CERRADO);
    }

    @Test
    void buscarConMetricas_filtraPorCursoId_y_AutorId() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso1 = crearCurso("Spring Boot", cat);
        var curso2 = crearCurso("JPA", cat);

        var autor1 = crearUsuario("u1", "U1");
        var autor2 = crearUsuario("u2", "U2");

        var t1 = crearTopico("A", "m", autor1, curso1, StatusTopico.ACTIVO, LocalDateTime.of(2025, 12, 10, 10, 0));
        var t2 = crearTopico("B", "m", autor2, curso1, StatusTopico.ACTIVO, LocalDateTime.of(2025, 12, 10, 10, 0));
        var t3 = crearTopico("C", "m", autor1, curso2, StatusTopico.ACTIVO, LocalDateTime.of(2025, 12, 10, 10, 0));

        flushAndClear();

        // Act: curso1 + autor1 => solo t1
        Page<DatosListadoTopico> page = topicoRepository.buscarConMetricas(
                null,
                curso1.getId(),
                autor1.getId(),
                null,
                null, null,
                null, null,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).titulo()).isEqualTo("A");
    }
}
