package com.alura.foro.hub.api.fixtures;

import com.alura.foro.hub.api.entity.enums.StatusTopico;
import com.alura.foro.hub.api.entity.model.Respuesta;
import com.alura.foro.hub.api.entity.model.RespuestaHija;
import com.alura.foro.hub.api.entity.model.Topico;
import com.alura.foro.hub.api.entity.model.Usuario;
import com.alura.foro.hub.api.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.repository.RespuestaRepository;
import com.alura.foro.hub.api.repository.TopicoRepository;
import com.alura.foro.hub.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class ForoHubFixtures {

    private final UsuarioRepository usuarioRepository;
    private final TopicoRepository topicoRepository;
    private final RespuestaRepository respuestaRepository;
    private final RespuestaHijaRepository respuestaHijaRepository;

    public ForoHubFixtures(
            UsuarioRepository usuarioRepository,
            TopicoRepository topicoRepository,
            RespuestaRepository respuestaRepository,
            RespuestaHijaRepository respuestaHijaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.topicoRepository = topicoRepository;
        this.respuestaRepository = respuestaRepository;
        this.respuestaHijaRepository = respuestaHijaRepository;
    }

    // ============
    // USUARIOS
    // ============
    public Usuario usuario(String username) {
        var u = new Usuario();
        u.setNombre(capitalizar(username));
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        u.setPassword("123");
        return usuarioRepository.saveAndFlush(u);
    }

    // ============
    // TÓPICOS
    // ============
    public Topico topico(Usuario autor) {
        return topico(autor, "Topico test", "Mensaje test", StatusTopico.ACTIVO);
    }

    public Topico topico(Usuario autor, String titulo, String mensaje, StatusTopico status) {
        var t = new Topico();
        t.setTitulo(titulo);
        t.setMensaje(mensaje);
        t.setAutor(autor);
        t.setStatus(status);
        return topicoRepository.saveAndFlush(t);
    }

    // ============
    // RESPUESTAS (PADRE)
    // ============
    public Respuesta respuesta(Topico topico, Usuario autor) {
        return respuesta(topico, autor, "Respuesta base");
    }

    public Respuesta respuesta(Topico topico, Usuario autor, String mensaje) {
        var r = new Respuesta();
        r.setMensaje(mensaje);
        r.setAutor(autor);
        r.setTopico(topico);
        return respuestaRepository.saveAndFlush(r);
    }

    // ============
    // RESPUESTAS HIJAS
    // ============
    public RespuestaHija respuestaHija(Respuesta respuestaPadre, Usuario autor) {
        return respuestaHija(respuestaPadre, autor, "Respuesta hija");
    }

    public RespuestaHija respuestaHija(Respuesta respuestaPadre, Usuario autor, String mensaje) {
        var rh = new RespuestaHija();
        rh.setMensaje(mensaje);
        rh.setAutor(autor);
        rh.setRespuesta(respuestaPadre);
        return respuestaHijaRepository.saveAndFlush(rh);
    }

    private String capitalizar(String s) {
        if (s == null || s.isBlank()) return "User";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public Topico topicoConFecha(Usuario autor, LocalDateTime fecha, StatusTopico status) {
        var t = topico(autor, "Topico test", "Mensaje test", status);
        t.setFechaCreacion(fecha);
        return topicoRepository.saveAndFlush(t);
    }

    public Respuesta respuestaConFecha(Topico topico, Usuario autor, LocalDateTime fecha) {
        var r = respuesta(topico, autor, "Respuesta base");
        r.setFechaCreacion(fecha);
        return respuestaRepository.saveAndFlush(r);
    }

    public Usuario usuarioConPassword(String username, String rawPassword, PasswordEncoder encoder) {
        var u = new Usuario();
        u.setNombre(capitalizar(username));
        u.setUsername(username);
        u.setEmail(username + "@test.com");
        u.setPassword(encoder.encode(rawPassword));
        return usuarioRepository.saveAndFlush(u);
    }

}
