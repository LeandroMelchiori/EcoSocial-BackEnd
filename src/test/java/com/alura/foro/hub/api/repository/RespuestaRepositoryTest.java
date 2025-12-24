package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RespuestaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired RespuestaRepository respuestaRepository;

    // ========= Helpers mínimos =========

    private Usuario crearUsuario(String username, String nombre) {
        Usuario u = new Usuario();
        // TODO: ajustá campos obligatorios reales (email, password, etc.)
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

    private Topico crearTopico(String titulo, Usuario autor, Curso curso, StatusTopico status, LocalDateTime fechaCreacion) {
        Topico t = new Topico();
        t.setTitulo(titulo);
        t.setMensaje("mensaje");
        t.setAutor(autor);
        t.setCurso(curso);
        t.setStatus(status);
        t.setFechaCreacion(fechaCreacion);
        em.persist(t);
        return t;
    }

    private Respuesta crearRespuesta(Topico topico, Usuario autor, String mensaje, boolean solucion, LocalDateTime fecha) {
        Respuesta r = new Respuesta();
        r.setTopico(topico);
        r.setAutor(autor);
        r.setMensaje(mensaje);
        r.setSolucion(solucion);
        r.setFechaCreacion(fecha);
        em.persist(r);
        return r;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    // =========================
    // desmarcarSoluciones
    // =========================
    @Test
    @Transactional
    void desmarcarSoluciones_poneFalseTodasLasSolucionesDelTopico() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var topico = crearTopico("T1", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        var r1 = crearRespuesta(topico, autorResp, "R1", true,  LocalDateTime.of(2025, 12, 10, 10, 0));
        var r2 = crearRespuesta(topico, autorResp, "R2", true,  LocalDateTime.of(2025, 12, 10, 11, 0));
        var r3 = crearRespuesta(topico, autorResp, "R3", false, LocalDateTime.of(2025, 12, 10, 12, 0));

        flushAndClear();

        // Act
        respuestaRepository.desmarcarSoluciones(topico.getId());
        flushAndClear();

        // Assert
        var todas = respuestaRepository.findByTopicoId(topico.getId(), Pageable.unpaged()).getContent();
        assertThat(todas).hasSize(3);
        assertThat(todas).allMatch(r -> Boolean.FALSE.equals(r.getSolucion()));
    }

    // =========================
    // Orden: solucion DESC, fechaCreacion DESC
    // =========================
    @Test
    void findByTopicoIdOrderBySolucionDescFechaCreacionDesc_ordenCorrecto() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var topico = crearTopico("T1", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        // Solucion=true (más nueva)
        var rSolNueva = crearRespuesta(topico, autorResp, "SOL NUEVA", true, LocalDateTime.of(2025, 12, 20, 10, 0));
        // Solucion=true (más vieja)
        var rSolVieja = crearRespuesta(topico, autorResp, "SOL VIEJA", true, LocalDateTime.of(2025, 12, 10, 10, 0));

        // Solucion=false (más nueva)
        var rNoSolNueva = crearRespuesta(topico, autorResp, "NO SOL NUEVA", false, LocalDateTime.of(2025, 12, 25, 10, 0));
        // Solucion=false (más vieja)
        var rNoSolVieja = crearRespuesta(topico, autorResp, "NO SOL VIEJA", false, LocalDateTime.of(2025, 12, 1, 10, 0));

        flushAndClear();

        // Act
        Page<Respuesta> page = respuestaRepository
                .findByTopicoIdOrderBySolucionDescFechaCreacionDesc(topico.getId(), PageRequest.of(0, 10));

        // Assert
        var msgs = page.getContent().stream().map(Respuesta::getMensaje).toList();

        // Primero todas las solucion=true (por fecha desc), luego solucion=false (por fecha desc)
        assertThat(msgs).containsExactly(
                "SOL NUEVA",
                "SOL VIEJA",
                "NO SOL NUEVA",
                "NO SOL VIEJA"
        );
    }

    // =========================
    // findByTopicoIdAndSolucionTrue
    // =========================
    @Test
    void findByTopicoIdAndSolucionTrue_devuelveSoloSolucionesDeEseTopico() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var t1 = crearTopico("T1", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        var t2 = crearTopico("T2", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        crearRespuesta(t1, autorResp, "T1-SOL", true, LocalDateTime.now());
        crearRespuesta(t1, autorResp, "T1-NO",  false, LocalDateTime.now());
        crearRespuesta(t2, autorResp, "T2-SOL", true, LocalDateTime.now());

        flushAndClear();

        // Act
        List<Respuesta> solucionesT1 = respuestaRepository.findByTopicoIdAndSolucionTrue(t1.getId());

        // Assert
        assertThat(solucionesT1).hasSize(1);
        assertThat(solucionesT1.get(0).getMensaje()).isEqualTo("T1-SOL");
        assertThat(solucionesT1.get(0).getTopico().getId()).isEqualTo(t1.getId());
        assertThat(solucionesT1.get(0).getSolucion()).isTrue();
    }

    // =========================
    // findBySolucionTrue
    // =========================
    @Test
    void findBySolucionTrue_devuelveTodasLasRespuestasMarcadasComoSolucion() {
        // Arrange
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var t1 = crearTopico("T1", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        var t2 = crearTopico("T2", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        crearRespuesta(t1, autorResp, "S1", true, LocalDateTime.now());
        crearRespuesta(t1, autorResp, "N1", false, LocalDateTime.now());
        crearRespuesta(t2, autorResp, "S2", true, LocalDateTime.now());

        flushAndClear();

        // Act
        List<Respuesta> sols = respuestaRepository.findBySolucionTrue();

        // Assert
        assertThat(sols).hasSize(2);
        assertThat(sols).allMatch(r -> Boolean.TRUE.equals(r.getSolucion()));
        assertThat(sols.stream().map(Respuesta::getMensaje).toList()).containsExactlyInAnyOrder("S1", "S2");
    }
}
