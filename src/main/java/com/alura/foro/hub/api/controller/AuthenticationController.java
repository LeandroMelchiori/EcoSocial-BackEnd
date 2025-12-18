package com.alura.foro.hub.api.controller;

import com.alura.foro.hub.api.entity.model.Usuario;
import jakarta.validation.Valid;
import com.alura.foro.hub.api.security.jwt.DatosJWTToken;
import com.alura.foro.hub.api.security.jwt.TokenService;
import com.alura.foro.hub.api.security.auth.UsuarioAuthenticateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity authenticateUser(
            @RequestBody @Valid UsuarioAuthenticateData userAuthenticateData) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(userAuthenticateData.username(),
                userAuthenticateData.password());

        var authenticatedUser = authenticationManager.authenticate(authToken);
        var JWTtoken = tokenService.generateToken((Usuario) authenticatedUser.getPrincipal());
        return ResponseEntity.ok(new DatosJWTToken(JWTtoken));
    }
}