package com.moveo.flight.api.service;

import com.moveo.flight.api.dto.UserDTO;
import com.moveo.flight.api.model.User;
import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Map;

public interface UserService {
    // UserService implementation will go here
    // This service will handle user-related operations such as registration, login, etc.
    
    // service interface for creating user
    Map<String, Object> createUser(User user) throws MessagingException;

    // service interface for getting user by id
    User getUserById(Long id);

    // service interface for getting all users
    List<User> getAllUsers();

    // service interface for updating user
    User updateUser(Long id, User updatedUser);

    // service interface for verifying user with OTP
    boolean verifyOtp(String email, String otpCode);

    // service interface for resending OTP
    void resendOtp(String email) throws MessagingException;

    public static UserDTO toDTO(User user) {
    return new UserDTO(
        user.getId(),
        user.getEmail(),
        user.getRoles(),
        user.getFullName(),
        user.getPhoneNumber(),
        user.getStatus(), null
        );
    }
}
