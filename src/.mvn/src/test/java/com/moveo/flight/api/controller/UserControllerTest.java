package com.moveo.flight.api.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moveo.flight.api.service.OtpService;
import com.moveo.flight.api.service.UserService;
import com.moveo.flight.api.dto.VerifyOtpDTO;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.model.UserStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OtpService otpService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUserEndpoint_Success() throws Exception{
        Map<String, Object> serviceResponse = new HashMap<>();
        // Create a mock user object
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("John Doe");
        mockUser.setPassword("test");

        // Mock the service response
        serviceResponse.put("success", true);
        serviceResponse.put("user", mockUser);

        // Mock the userService and otpService
        Mockito.when(userService.getUserById(1L)).thenReturn(mockUser);
        Mockito.when(userService.createUser(any(User.class))).thenReturn(serviceResponse);
        Mockito.when(otpService.sendOtp(any(User.class), anyString())).thenReturn(true);

        // Perform the POST request
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.message").value("User registered. OTP sent to phone/email for verification"));
    }

    @Test
    void testVerifyOtpEndpoint_Success() throws Exception{
        // Create a mock request body
        VerifyOtpDTO req = new VerifyOtpDTO();
        req.setEmail("test@example.com");
        req.setOtpCode("123456");

        // Mock the service response
        Mockito.when(userService.verifyOtp(req.getEmail(), req.getOtpCode())).thenReturn(true);

        // Perform the POST request
        mockMvc.perform(post("/api/users/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User verified successfully."));
    }

    @Test
    void testResendOtpEndpoint_Success() throws Exception{
        String email = "test@example.com";
        
        // Mock the service response
        Mockito.doNothing().when(userService).resendOtp(email);

        // Perform the GET request
        mockMvc.perform(get("/api/users/resend-otp")
                        .param("email", email))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OTP resent to " + email));
    }

    @Test
    void testGetUserByIdEndpoint_Success() throws Exception{
        // Create a mock user object
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Mock the service response
        Mockito.when(userService.getUserById(1L)).thenReturn(mockUser);

        // Perform the GET request
        mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetAllUsersEndpoint_Success() throws Exception{
        // Create a mock user object
        User mockUser1 = new User();
        mockUser1.setId(1L);
        mockUser1.setEmail("test@example.com");
        User mockUser2 = new User();
        mockUser2.setId(2L);
        mockUser2.setEmail("test2@example.com");
        List<User> mockUsers = Arrays.asList(mockUser1, mockUser2);

        // Mock the service response
        Mockito.when(userService.getAllUsers()).thenReturn(mockUsers);

        // Perform the GET request
        mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testUpdateUserEndpoint_Success() throws Exception{
        // Create a mock user object
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("John Doe Updated");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setPassword("newpassword");
        mockUser.setRoles(UserRole.ADMIN);
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setOtpCode(null);

        // Mock the service response
        Mockito.when(userService.updateUser(eq(1L), any(User.class))).thenReturn(mockUser);
        
        // Perform the PUT request
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
