package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RespuestaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    RespuestaRepository respuestaRepository;

    /* ======================
       Helpers con auditoría
       ====================== */

    private Usuario crearUsuario(String username, String nombre) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(nombre);
        u.setEmail(username + "@test.com");
        u.setPassword("123456");

        auditar(u);
        em.persist(u);
        return u;
    }

    private Categoria crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);

        auditar(c);
        em.persist(c);
        return c;
    }

    private Curso crearCurso(String nombre, Categoria categoria) {
        Curso c = new Curso();
        c.setNombre(nombre);
        c.setCategoria(categoria);

        auditar(c);
        em.persist(c);
        return c;
    }

    private Topico crearTopico(
            String titulo,
            Usuario autor,
            Curso curso,
            StatusTopico status,
            LocalDateTime fechaCreacion
    ) {
        Topico t = new Topico();
        t.setTitulo(titulo);
        t.setMensaje("mensaje");
        t.setAutor(autor);
        t.setCurso(curso);
        t.setStatus(status);
        t.setFechaCreacion(fechaCreacion);

        auditar(t);
        em.persist(t);
        return t;
    }

    private Respuesta crearRespuesta(
            Topico topico,
            Usuario autor,
            String mensaje,
            boolean solucion,
            LocalDateTime fecha
    ) {
        Respuesta r = new Respuesta();
        r.setTopico(topico);
        r.setAutor(autor);
        r.setMensaje(mensaje);
        r.setSolucion(solucion);
        r.setFechaCreacion(fecha);

        auditar(r);
        em.persist(r);
        return r;
    }

    private void auditar(Object entity) {
        if (entity instanceof Categoria c) {
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
        }
        if (entity instanceof Curso c) {
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
        }
        if (entity instanceof Topico t) {
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
        }
        if (entity instanceof Respuesta r) {
            r.setCreatedAt(LocalDateTime.now());
            r.setUpdatedAt(LocalDateTime.now());
        }
        if (entity instanceof Usuario u) {
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    /* ======================
       Tests
       ====================== */

    @Test
    @Transactional
    void desmarcarSoluciones_poneFalseTodasLasSolucionesDelTopico() {
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var topico = crearTopico(
                "T1",
                autorTopico,
                curso,
                StatusTopico.ACTIVO,
                LocalDateTime.now()
        );

        crearRespuesta(topico, autorResp, "R1", true,  LocalDateTime.of(2025, 12, 10, 10, 0));
        crearRespuesta(topico, autorResp, "R2", true,  LocalDateTime.of(2025, 12, 10, 11, 0));
        crearRespuesta(topico, autorResp, "R3", false, LocalDateTime.of(2025, 12, 10, 12, 0));

        flushAndClear();

        respuestaRepository.desmarcarSoluciones(topico.getId());
        flushAndClear();

        var todas = respuestaRepository
                .findByTopicoId(topico.getId(), Pageable.unpaged())
                .getContent();

        assertThat(todas).hasSize(3);
        assertThat(todas).allMatch(r -> Boolean.FALSE.equals(r.getSolucion()));
    }

    @Test
    void findByTopicoIdOrderBySolucionDescFechaCreacionDesc_ordenCorrecto() {
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var topico = crearTopico(
                "T1",
                autorTopico,
                curso,
                StatusTopico.ACTIVO,
                LocalDateTime.now()
        );

        crearRespuesta(topico, autorResp, "SOL NUEVA", true,
                LocalDateTime.of(2025, 12, 20, 10, 0));
        crearRespuesta(topico, autorResp, "SOL VIEJA", true,
                LocalDateTime.of(2025, 12, 10, 10, 0));
        crearRespuesta(topico, autorResp, "NO SOL NUEVA", false,
                LocalDateTime.of(2025, 12, 25, 10, 0));
        crearRespuesta(topico, autorResp, "NO SOL VIEJA", false,
                LocalDateTime.of(2025, 12, 1, 10, 0));

        flushAndClear();

        Page<Respuesta> page =
                respuestaRepository.findByTopicoIdOrderBySolucionDescFechaCreacionDesc(
                        topico.getId(),
                        PageRequest.of(0, 10)
                );

        var mensajes = page.getContent()
                .stream()
                .map(Respuesta::getMensaje)
                .toList();

        assertThat(mensajes).containsExactly(
                "SOL NUEVA",
                "SOL VIEJA",
                "NO SOL NUEVA",
                "NO SOL VIEJA"
        );
    }

    @Test
    void findByTopicoIdAndSolucionTrue_devuelveSoloSolucionesDeEseTopico() {
        var cat = crearCategoria("Backend");
        var curso = crearCurso("Spring", cat);

        var autorTopico = crearUsuario("autor", "Autor");
        var autorResp = crearUsuario("resp", "Resp");

        var t1 = crearTopico("T1", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());
        var t2 = crearTopico("T2", autorTopico, curso, StatusTopico.ACTIVO, LocalDateTime.now());

        crearRespuesta(t1, autorResp, "T1-SOL", true, LocalDateTime.now());
        crearRespuesta(t1, autorResp, "T1-NO", false, LocalDateTime.now());
        crearRespuesta(t2, autorResp, "T2-SOL", true, LocalDateTime.now());

        flushAndClear();

        List<Respuesta> soluciones = respuestaRepository.findByTopicoIdAndSolucionTrue(t1.getId());

        assertThat(soluciones).hasSize(1);
        assertThat(soluciones.get(0).getMensaje()).isEqualTo("T1-SOL");
        assertThat(soluciones.get(0).getSolucion()).isTrue();
    }

    @Test
    void findBySolucionTrue_devuelveTodasLasRespuestasMarcadasComoSolucion() {
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

        List<Respuesta> soluciones = respuestaRepository.findBySolucionTrue();

        assertThat(soluciones).hasSize(2);
        assertThat(soluciones).allMatch(r -> Boolean.TRUE.equals(r.getSolucion()));
        assertThat(soluciones.stream().map(Respuesta::getMensaje).toList())
                .containsExactlyInAnyOrder("S1", "S2");
    }
}

