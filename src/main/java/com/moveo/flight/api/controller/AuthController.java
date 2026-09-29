package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.AuthRequest;
import com.moveo.flight.api.dto.AuthResponse;
import com.moveo.flight.api.security.JwtUtil;
import com.moveo.flight.api.service.AuthService;
import com.moveo.flight.api.service.TokenBlacklistService;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    @Autowired
    public AuthController(AuthService authService, TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.jwtService = new JwtUtil();
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    /**
     * POST /api/auth/logout
     * Invalidate the JWT token.
     * @return 
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        // Extraire la date a laquelle le token expire
        LocalDateTime expiry = jwtService.extractExpiration(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        tokenBlacklistService.blacklistToken(token, expiry);
        return ResponseEntity.ok("Logged out successfully");
        }
        // Si pas de token passer dans le header ou mauvais token passer dans le header
        return ResponseEntity.badRequest().body("Invalid token");
    }
}

