package com.moveo.flight.api.controller;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moveo.flight.api.service.AuthService;
import com.moveo.flight.api.dto.AuthRequest;
import com.moveo.flight.api.dto.AuthResponse;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.moveo.flight.api.service.TokenBlacklistService tokenBlacklistService;

    @Test
    void testLoginEndpoint_Success() throws Exception {
        // mock the request payload
        String requestPayload = "{ \"email\": \"test@example.com\", \"password\": \"password123\" }";
        String token = "mock-jwt-token";

        AuthResponse mockResponse = new AuthResponse(token, "User ID : 1 , logged in successfully");
        // mock the service response
        Mockito.when(authService.login(any(AuthRequest.class))).thenReturn(mockResponse);

        // perform the POST request and verify the response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.message").value("User ID : 1 , logged in successfully"));
    }
}
