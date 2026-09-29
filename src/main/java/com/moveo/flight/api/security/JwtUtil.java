package com.moveo.flight.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Utility class for generating and validating JWT tokens.
 * Used for authenticating users in the API.
 */
@Component // Spring can inject it and manage it as a bean
public class JwtUtil {

    // Secret key for signing the JWT (in production, store in ENV variable)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token validity = 1 hour
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 1;

    /** Generate a JWT token for the given email */
    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /** Extract email (subject) from token */
    public static String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /** Validate token (check signature + expiration) */
    public static boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Helper to get claims */
    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Extract expiration date from token */
    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }
}

