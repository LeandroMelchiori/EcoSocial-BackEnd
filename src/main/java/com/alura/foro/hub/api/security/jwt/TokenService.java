package com.alura.foro.hub.api.security.jwt;

import com.alura.foro.hub.api.user.domain.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.auth0.jwt.algorithms.Algorithm.HMAC256;

@Service
public class TokenService {

    @Value(value = "${api.security.secret}")
    private String apiSecret;

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
            DecodedJWT decodedJWT = JWT
                    .require(algorithm)
                    .withIssuer("foro hub")
                    .build()
                    .verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Error al verificar el token", exception);
        }
    }

    private Instant generateExpirationTime() {
        return Instant.now().plus(2, ChronoUnit.HOURS);
    }

    public Long getUserId(String token) {
        try {
            DecodedJWT verifier = JWT.require(Algorithm.HMAC256(apiSecret))
                    .withIssuer("foro hub")
                    .build()
                    .verify(token);

            return verifier.getClaim("id").asLong();

        } catch (Exception e) {
            throw new RuntimeException("Error al verificar el token");
        }
    }
}
