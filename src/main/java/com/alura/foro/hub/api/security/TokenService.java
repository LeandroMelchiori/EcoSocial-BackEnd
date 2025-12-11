package com.alura.foro.hub.api.security;

import com.alura.foro.hub.api.domain.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.auth0.jwt.algorithms.Algorithm.HMAC256;

@Service
public class TokenService {

    @Value("${api.security.secret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        System.out.println("API Secret: " + apiSecret);
    }

    public String generateToken(Usuario usuario) {
        try {
            Algorithm algorithm = HMAC256(apiSecret);
            return JWT.create()
                    .withIssuer("foro hub")
                    .withSubject(usuario.getUsername())
                    .withClaim("id", usuario.getId())
                    .withExpiresAt(generateExpirationTime())
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            // Invalid Signing configuration / Couldn't convert Claims.
            throw new RuntimeException();
        }
    }
    public String  getSubject(String token) {
        if (token == null) {
            throw new RuntimeException();
        }
        try {
            Algorithm algorithm = HMAC256(apiSecret);
            DecodedJWT verifier = JWT.require(algorithm)
                    // specify any specific claim validations
                    .withIssuer("foro hub")
                    // reusable verifier instance
                    .build()
                    .verify(token);
           return verifier.getSubject();
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Error al verificar el token", exception);
        }
    }

    private Instant generateExpirationTime() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.UTC);
    }
}
