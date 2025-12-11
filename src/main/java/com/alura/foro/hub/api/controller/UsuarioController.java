//package com.alura.foro.hub.api.controller;
//
//import com.alura.foro.hub.api.domain.Usuario;
//import com.alura.foro.hub.api.service.UsuarioService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/usuarios")
//public class UsuarioController {
//
//    private final UsuarioService usuarioService;
//
//    public UsuarioController(UsuarioService usuarioService) {
//        this.usuarioService = usuarioService;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Usuario>> listarUsuarios() {
//        return ResponseEntity.ok(usuarioService.listarUsuarios());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
//        Optional<Usuario> usuario = usuarioService.obtenerPorId(id);
//        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Usuario> crearUsuario(@RequestBody @Valid Usuario usuario) {
//        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
//        return ResponseEntity.ok(nuevoUsuario);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody @Valid Usuario usuario) {
//        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
//        return ResponseEntity.ok(usuarioActualizado);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
//        usuarioService.eliminarUsuario(id);
//        return ResponseEntity.noContent().build();
//    }
//}
