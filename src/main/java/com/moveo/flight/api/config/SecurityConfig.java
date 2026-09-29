package com.moveo.flight.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.moveo.flight.api.security.CustomUserDetailsService;
import com.moveo.flight.api.security.JwtAuthenticationFilter;
import com.moveo.flight.api.security.JwtUtil;
import com.moveo.flight.api.service.TokenBlacklistService;

/**
 * SecurityConfig defines which endpoints are public
 * and which require authentication.
 * 
 * For now, we allow free access to user registration
 * and disable CSRF for API usage.
 */
@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, 
                          TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ✅ Create JWT filter
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService, tokenBlacklistService);

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Everyone endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/flights/search").permitAll()
                .requestMatchers("/api/users/verify-otp").permitAll()
                .requestMatchers("/api/users/resend-otp").permitAll()
                .requestMatchers("/api/auth/password/**").permitAll()
                .requestMatchers("/api/flights").permitAll()
                // Admin-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/routes/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                // Payment verificaiton endpoint
                .requestMatchers(HttpMethod.POST, "/api/payments/**").permitAll()
                .requestMatchers("/api/payments/stripe/webhook").permitAll()
                // Everything else needs authentication
                .anyRequest().authenticated()
            )
            // Add JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

