package com.alura.foro.hub.api.repository;

import com.alura.foro.hub.api.dto.topico.DatosListadoTopico;
import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TopicoRepositoryTest {

    @Autowired TopicoRepository topicoRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("listarConMetricas: devuelve métricas (cantidadRespuestas y fechaUltimaRespuesta)")
    void listarConMetricas_devuelveMetricas() {
        // Arrange
        var cat = new Categoria();
        cat.setNombre("Economía social");
        em.persist(cat);

        var curso = new Curso();
        curso.setNombre("Emprendimientos digitales");
        curso.setCategoria(cat);
        em.persist(curso);

        var autor = new Usuario();
        autor.setNombre("Leandro");
        autor.setUsername("lean");
        autor.setPassword("123"); // da igual en repo test
        em.persist(autor);

        var topico = new Topico();
        topico.setTitulo("Consulta sobre monotributo");
        topico.setMensaje("¿Cómo facturo si vendo por Instagram?");
        topico.setFechaCreacion(LocalDateTime.now().minusDays(1));
        topico.setStatus(StatusTopico.ACTIVO);
        topico.setAutor(autor);
        topico.setCurso(curso);
        em.persist(topico);

        var r1 = new Respuesta();
        r1.setMensaje("Respuesta 1");
        r1.setFechaCreacion(LocalDateTime.now().minusHours(10));
        r1.setAutor(autor);
        r1.setTopico(topico);
        em.persist(r1);

        var r2 = new Respuesta();
        r2.setMensaje("Respuesta 2");
        r2.setFechaCreacion(LocalDateTime.now().minusHours(2));
        r2.setAutor(autor);
        r2.setTopico(topico);
        em.persist(r2);

        em.flush();
        em.clear();

        // Act
        Page<DatosListadoTopico> page = topicoRepository.listarConMetricas(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "fechaCreacion"))
        );

        // Assert
        assertThat(page.getContent()).hasSize(1);

        DatosListadoTopico dto = page.getContent().get(0);
        assertThat(dto.cantidadRespuestas()).isEqualTo(2);
        assertThat(dto.fechaUltimaRespuesta()).isNotNull();
        assertThat(dto.fechaUltimaRespuesta()).isAfter(LocalDateTime.now().minusHours(3));
    }

    @Test
    @DisplayName("buscarConMetricas: filtra por q + status + cursoId")
    void buscarConMetricas_filtra() {
        // Arrange: data mínima (2 tópicos, 1 matchea)
        var cat = new Categoria(); cat.setNombre("Backend"); em.persist(cat);
        var curso = new Curso(); curso.setNombre("Spring"); curso.setCategoria(cat); em.persist(curso);

        var autor = new Usuario();
        autor.setNombre("Sacha");
        autor.setUsername("sacha");
        autor.setPassword("123");
        em.persist(autor);

        var ok = new Topico();
        ok.setTitulo("Error con Spring Security");
        ok.setMensaje("No me anda el filtro");
        ok.setFechaCreacion(LocalDateTime.now().minusDays(2));
        ok.setStatus(StatusTopico.ACTIVO);
        ok.setAutor(autor);
        ok.setCurso(curso);
        em.persist(ok);

        var no = new Topico();
        no.setTitulo("Cualquier cosa");
        no.setMensaje("Nada que ver");
        no.setFechaCreacion(LocalDateTime.now().minusDays(2));
        no.setStatus(StatusTopico.CERRADO);
        no.setAutor(autor);
        no.setCurso(curso);
        em.persist(no);

        em.flush();
        em.clear();

        // OJO: acá depende de tu firma.
        // Si tu repo usa List<StatusTopico> -> pasamos List.of(StatusTopico.ABIERTO)
        // Si tu repo ya quedó con StatusTopico simple -> pasá StatusTopico.ABIERTO y ajustá el método.
        var page = topicoRepository.buscarConMetricas(
                "security",
                curso.getId(),
                autor.getId(),
                StatusTopico.ACTIVO,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).titulo()).containsIgnoringCase("security");
        assertThat(page.getContent().get(0).status()).isEqualTo(StatusTopico.ACTIVO);
    }
}
