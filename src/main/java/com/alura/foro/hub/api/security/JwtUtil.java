//package com.alura.foro.hub.api.security;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    @Value("${api.security.secret}")
//    private String secretKey;
//
//    @Value("${jwt.expiration}")
//    private long expirationTime;
//
//    public String generateToken(String username) {
//        return JWT.create()
//                .withSubject(username)
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
//                .sign(Algorithm.HMAC256(secretKey));
//    }
//
//    public String extractUsername(String token) {
//        try {
//            return JWT.require(Algorithm.HMAC256(secretKey))
//                    .build()
//                    .verify(token)
//                    .getSubject();
//        } catch (JWTVerificationException e) {
//            return null;
//        }
//    }
//
//    public boolean validateToken(String token, String username) {
//        return username.equals(extractUsername(token)) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        Date expiration = JWT.require(Algorithm.HMAC256(secretKey))
//                .build()
//                .verify(token)
//                .getExpiresAt();
//        return expiration.before(new Date());
//    }
//}
