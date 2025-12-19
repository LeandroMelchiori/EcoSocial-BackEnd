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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import com.alura.foro.hub.api.security.exception.ApiResponsesDefault;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Operation(
            summary = "Login de usuario",
            description = "Autentica un usuario y devuelve un token JWT"
    )
    @ApiResponsesDefault
    @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DatosJWTToken.class),
                    examples = @ExampleObject(
                            name = "Login OK",
                            value = """
                            {
                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                            }
                            """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsuarioAuthenticateData.class),
                    examples = @ExampleObject(
                            name = "Login",
                            value = """
                        {
                          "username": "user",
                          "password": "123456"
                        }
                        """
                    )
            )
    )
    @PostMapping("/login")
    public ResponseEntity<DatosJWTToken> authenticateUser(
            @RequestBody @Valid UsuarioAuthenticateData userAuthenticateData) {

        Authentication authToken =
                new UsernamePasswordAuthenticationToken(
                        userAuthenticateData.username(),
                        userAuthenticateData.password()
                );

        var authenticatedUser = authenticationManager.authenticate(authToken);
        var JWTtoken = tokenService.generateToken((Usuario) authenticatedUser.getPrincipal());

        return ResponseEntity.ok(new DatosJWTToken(JWTtoken));
    }
}
