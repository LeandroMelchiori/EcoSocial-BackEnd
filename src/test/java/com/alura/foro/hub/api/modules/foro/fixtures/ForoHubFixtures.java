package com.alura.foro.hub.api.modules.foro.fixtures;

import com.alura.foro.hub.api.modules.foro.domain.enums.StatusTopico;
import com.alura.foro.hub.api.modules.foro.domain.model.Respuesta;
import com.alura.foro.hub.api.modules.foro.domain.model.RespuestaHija;
import com.alura.foro.hub.api.modules.foro.domain.model.Topico;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaHijaRepository;
import com.alura.foro.hub.api.modules.foro.repository.RespuestaRepository;
import com.alura.foro.hub.api.modules.foro.repository.TopicoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
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
    public Usuario usuario(String login) {
        var u = new Usuario();

        // nombre / apellido obligatorios
        u.setNombre(capitalizar(login));
        u.setApellido("Test");

        // dni obligatorio (y único)
        u.setDni(dniDesde(login));

        // email obligatorio (y único)
        u.setEmail(emailDesde(login));

        // password obligatorio
        u.setPassword("123");

        return usuarioRepository.saveAndFlush(u);
    }

    public Usuario usuarioConPassword(String login, String rawPassword, PasswordEncoder encoder) {
        var u = new Usuario();

        u.setNombre(capitalizar(login));
        u.setApellido("Test");
        u.setDni(dniDesde(login));
        u.setEmail(emailDesde(login));
        u.setPassword(encoder.encode(rawPassword));

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

    // Helpers
    private String emailDesde(String login) {
        if (login == null || login.isBlank()) return "user@test.com";
        return login.contains("@") ? login : login + "@test.com";
    }

    private String dniDesde(String login) {
        // si ya viene DNI numérico, lo usamos
        if (login != null && login.matches("\\d{7,20}")) return login;

        // si viene tipo "sacha" o "user1", generamos uno estable (pero numérico)
        int base = Math.abs((login == null ? "user" : login).hashCode());
        // 8 dígitos (evita colisiones fáciles)
        return String.format("%08d", base % 100_000_000);
    }
}
