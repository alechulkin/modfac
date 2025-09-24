package com.example.modfac.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey secretKey;

    /**
     * Create JWT token
     */
    public String createToken(String username, String role) {
        SecretKey key = getSecretKey();


        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username); // subject claim
        claims.put("role", role);    // custom role claim

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();

//        Claims claims = Jwts.claims().subject(username).build();
////        claims.put("role", role);
//        claims.put("authorities", List.of("ROLE_" + role.toUpperCase()));
//
//
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + jwtExpiration);
//
//        return Jwts.builder().claims(claims).issuedAt(now).expiration(validity)
//                .signWith(key, Jwts.SIG.HS256)
//                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = getSecretKey();

            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get username from JWT token
     */
    public String getUsername(String token) {
        SecretKey key = getSecretKey();

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Get user role from JWT token
     */
    public String getRole(String token) {
        SecretKey key = getSecretKey();

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_16));
        }
        return secretKey;
    }
}