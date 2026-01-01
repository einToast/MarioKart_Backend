package de.fsr.mariokart_backend.user.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JWTManagerService {

    private final UserProperties userProperties;

    private SecretKey signingKey() {
        String base64 = userProperties.getSecretKey();

        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException("SECRET_KEY not set");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(base64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("SECRET_KEY is not valid Base64", ex);
        }

        // HS256: mindestens 256 Bit = 32 Bytes
        if (keyBytes.length < 32) {
            throw new IllegalStateException("SECRET_KEY too short, must be at least 32 bytes");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJWT(User user) {
        long expiresMs = userProperties.getExpiresAfter() * 60L * 60L * 1000L;

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer("CodeJava")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiresMs))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateJWT(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.err.println("JWT expired: " + ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            System.err.println("JWT invalid: " + ex.getMessage());
        }
        return false;
    }

    public String getSubjectFromToken(String token) {
        return parseJWTClaims(token).getSubject();
    }

    private Claims parseJWTClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}