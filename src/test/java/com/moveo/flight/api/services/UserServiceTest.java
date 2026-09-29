package com.moveo.flight.api.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.model.UserStatus;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.service.OtpService;
import com.moveo.flight.api.service.impl.UserServiceImpl;

import jakarta.mail.MessagingException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

public class UserServiceTest {
    
    private User authenticatedUser;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext SecurityContext;

    @InjectMocks
    private UserServiceImpl userServiceImpl; // Implementation, not interface

    public UserServiceTest(){
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContextHolder
        SecurityContextHolder.setContext(SecurityContext);
        when(SecurityContext.getAuthentication()).thenReturn(authentication);

        authenticatedUser = new User();
        authenticatedUser.setEmail("test@moveo.com");
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(UserRole.ADMIN);
        when(authentication.getName()).thenReturn("test@moveo.com");

    }

    @Test
    void testCreateUser_Success() throws MessagingException{
        // Create a User object
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setPassword("test");

        // Mock respository save
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Mock OTP service to simulate success
        when(otpService.sendOtp(any(User.class), anyString())).thenReturn(true);

        Map<String, Object> result = userServiceImpl.createUser(user);

        assertTrue((boolean) result.get("success"));
        assertNotNull(result.get("success"));
        assertEquals("test@example.com", ((User) result.get("user")).getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testGetUserById_Success() {
        // Mock user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@moveo.com");

        when(userRepository.findByEmail("test@moveo.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Call the service method
        User foundUser = userServiceImpl.getUserById(1L);

        // Verify the user details
        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }

    @Test
    void testGetAllUsers_Success() {
        // Mock users
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("test@moveo.com");
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("test2@moveo.com");
        List<User> users = List.of(user1, user2);

        // Mock repository responses
        when(userRepository.findAll()).thenReturn(users);

        // Call the service method
        List<User> foundUsers = userServiceImpl.getAllUsers();

        // Verify the results
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
    }

    @Test
    void testUpdateUser_Success() {
        // Mock user
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@moveo.com");
        existingUser.setFullName("John Doe");
        existingUser.setPhoneNumber("1234");
        existingUser.setRoles(UserRole.USER);

        // Mock updated user
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFullName("Jane Doe");
        updatedUser.setPhoneNumber("5678");
        updatedUser.setRoles(UserRole.ADMIN);
        updatedUser.setPassword("Password");

        // Mock repository responses
        when(userRepository.findByEmail(authenticatedUser.getEmail())).thenReturn(Optional.of(authenticatedUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Call the service method
        User result = userServiceImpl.updateUser(1L, updatedUser);

        // Verify the results
        assertNotNull(result);
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("5678", result.getPhoneNumber());
        assertEquals(UserRole.ADMIN, result.getRoles());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testVerifyOtp_Success() {
        // Mock user with OTP
        User user = new User();
        user.setId(1L);
        user.setOtpCode("123456");
        user.setEmail("test@moveo.com");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        // Mock repository response
        when(userRepository.findByEmail("test@moveo.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Call the service method
        boolean isVerified = userServiceImpl.verifyOtp("test@moveo.com", "123456");

        // Verify the results
        assertTrue(isVerified);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNull(user.getOtpCode());
        assertNull(user.getOtpExpiry());
    }
}
