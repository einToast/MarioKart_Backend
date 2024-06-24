package de.fsr.mariokart_backend.user.service;

import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResponseDTO;
import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class JWTManagerService {

    private final UserProperties userProperties;


    public String generateJWT(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer("CodeJava")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + userProperties.getExpiresAfter() * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, userProperties.getSecretKey())
                .compact();
    }


    public boolean validateJWT(String token) {
        try {
            Jwts.parser().setSigningKey(userProperties.getSecretKey()).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.err.println("JWT expired" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("Token is null, empty or only whitespace" + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.err.println("JWT is invalid" + ex);
        } catch (UnsupportedJwtException ex) {
            System.err.println("JWT is not supported" + ex);
        } catch (SignatureException ex) {
            System.err.println("Signature validation failed");
        }

        return false;
    }

    public String getSubjectFromToken(String token) {
        return parseJWTClaims(token).getSubject();
    }

    private Claims parseJWTClaims(String token) {
        return Jwts.parser()
                .setSigningKey(userProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}