//package com.alura.foro.hub.api.controller;
//
//import com.alura.foro.hub.api.domain.Perfil;
//import com.alura.foro.hub.api.service.PerfilService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/perfiles")
//public class PerfilController {
//
//    private final PerfilService perfilService;
//
//    @Autowired
//    public PerfilController(PerfilService perfilService) {
//        this.perfilService = perfilService;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Perfil>> obtenerTodosLosPerfiles() {
//        return ResponseEntity.ok(perfilService.obtenerTodosLosPerfiles());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Perfil> obtenerPerfilPorId(@PathVariable Long id) {
//        Optional<Perfil> perfil = perfilService.obtenerPerfilPorId(id);
//        return perfil.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<Perfil> crearPerfil(@RequestBody Perfil perfil) {
//        return ResponseEntity.ok(perfilService.crearPerfil(perfil));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Perfil> actualizarPerfil(@PathVariable Long id, @RequestBody Perfil perfil) {
//        Perfil perfilActualizado = perfilService.actualizarPerfil(id, perfil);
//        if (perfilActualizado != null) {
//            return ResponseEntity.ok(perfilActualizado);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> eliminarPerfil(@PathVariable Long id) {
//        perfilService.eliminarPerfil(id);
//        return ResponseEntity.noContent().build();
//    }
//}