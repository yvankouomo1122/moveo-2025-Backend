package com.moveo.flight.api.services;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.moveo.flight.api.dto.AuthRequest;
import com.moveo.flight.api.dto.AuthResponse;
import com.moveo.flight.api.service.AuthService;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserStatus;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.security.JwtUtil;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

public class AuthServiceTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    public AuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginUser_Success() {
        BCryptPasswordEncoder pEncoder = new BCryptPasswordEncoder();
        // Create Auth Request
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Mock User object and UserRepository
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(pEncoder.encode("password123"));
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(user));

        // Mock password match
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .thenReturn(true);

        // Mock static method JwtUtil.generateToken
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(user.getEmail()))
                .thenReturn("mocked-jwt-token");

            // Call the login method
            AuthResponse response = authService.login(request);

            // Verify the response
            assertNotNull(response.getToken(), "Token should not be null");
            assertEquals("mocked-jwt-token", response.getToken());
        }
    }

    @Test
    void testLoginUser_Failure_WrongPassword() {
        BCryptPasswordEncoder pEncoder = new BCryptPasswordEncoder();
        // Create Auth Request
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        // Mock User object and UserRepository
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(pEncoder.encode("password123"));
        when(userRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(user));

        // Mock password match
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .thenReturn(true);

        // Mock static method JwtUtil.generateToken
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(user.getEmail()))
                .thenReturn("mocked-jwt-token");

            // Call the login method
            AuthResponse response = authService.login(request);

            // Verify the response
            assertNull(response.getToken(), "Token should be null");
            assertEquals("Invalid credentials", response.getMessage());
        }
    }

    @Test
    void testLoginUser_Failure_UserNotFound() {
        // Create Auth Request
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        // Mock UserRepository
        when(userRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.empty());

        // Act and assert
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.login(request)
        );

        // Verify the response
        assertEquals(404, ex.getStatusCode().value());
        assertEquals("User not found", ex.getReason());
    }
}
