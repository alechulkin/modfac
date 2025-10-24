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
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey secretKey;

    /**
     * Create JWT token
     */
    /**
         * Create JWT token
         */
        public String createToken(String username, String role) {
        LOG.debug("createToken method invoked");
    
        SecretKey key = getSecretKey();
    
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username); // subject claim
        claims.put("role", role);    // custom role claim
    
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpiration);
    
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    
        LOG.debug("createToken method finished");
        return token;
    }

    /**
     * Validate JWT token
     */
    /**
         * Validate JWT token
         */
        public boolean validateToken(String token) {
        LOG.debug("validateToken method invoked");
        try {
            SecretKey key = getSecretKey();
    
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
    
            boolean isValid = !claims.getPayload().getExpiration().before(new Date());
            LOG.debug("validateToken method finished");
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            LOG.debug("validateToken method finished");
            return false;
        }
    }

    /**
     * Get username from JWT token
     */
    /**
         * Get username from JWT token
         */
        public String getUsername(String token) {
        LOG.debug("getUsername method invoked");
    
        SecretKey key = getSecretKey();
    
        String username = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    
        LOG.debug("getUsername method finished");
        return username;
    }

    /**
     * Get user role from JWT token
     */
    /**
         * Get user role from JWT token
         */
        public String getRole(String token) {
        LOG.debug("getRole method invoked");
    
        SecretKey key = getSecretKey();
    
        String role = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    
        LOG.debug("getRole method finished");
        return role;
    }

    /**
         * Get the secret key for signing JWT tokens
         */
        private SecretKey getSecretKey() {
        LOG.debug("getSecretKey method invoked");
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_16));
        }
        LOG.debug("getSecretKey method finished");
        return secretKey;
    }
}