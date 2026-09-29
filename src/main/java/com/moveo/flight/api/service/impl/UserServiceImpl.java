package com.moveo.flight.api.service.impl;

import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.model.UserStatus;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.service.EmailService;
import com.moveo.flight.api.service.OperationLogService;
import com.moveo.flight.api.service.SmsService;
import com.moveo.flight.api.service.UserService;
import com.moveo.flight.api.service.OtpService;

import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    protected final OtpService otpService;
    private final EmailService emailService;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    // Constructor injection for UserRepository, EmailService, SmsService, and OtpService
    public UserServiceImpl(UserRepository userRepository, 
    EmailService emailService, 
    SmsService smsService, 
    OtpService otpService,
    OperationLogRepository logRepository) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /**
     * Create a new user, checking email uniqueness.
     * @throws MessagingException if email sending fails
     */
    @Override
    public Map<String, Object> createUser(User user) throws MessagingException {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByEmail(user.getEmail())) {
            response.put("success", false);
            response.put("message", "Email already exists");
            response.put("user", null);
            return response;
        }
        // Create user with PENDING_VERIFICATION status
        User u = new User();
        u.setEmail(user.getEmail());
        u.setPhoneNumber(user.getPhoneNumber());
        u.setRoles(user.getRoles());
        u.setFullName(user.getFullName());
        u.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the password before saving
        u.setStatus(UserStatus.PENDING_VERIFICATION); // Set initial status

        // Generate OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        u.setOtpCode(otp);
        u.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // Set OTP expiry to 5 minutes from now

        // Send OTP via email and SMS
        boolean sent = otpService.sendOtp(u, otp);
        boolean user_created;
        if(!sent) {
            user_created = false;
            response.put("success", false);
            response.put("message", "Failed to send OTP");
            response.put("status", user_created);
            response.put("user", null);
            operationLogService.logAction("USER_CREATION_FAILED", "USER", null, null, "User creation failed");
            return response; // Return false if OTP sending failed
        }
        user_created = true;
        userRepository.save(u);
        operationLogService.logAction("USER_CREATED_SUCCESSFULLY", "USER", u.getId(), u.getEmail(), "User created successfully");
        response.put("success", user_created);
        response.put("user", u);
        return response; // Return true if user created successfully
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
                
        if (u.getOtpExpiry().isBefore(LocalDateTime.now())){
            operationLogService.logAction("USER_VERIFICATION_FAILED", "USER", u.getId(), u.getEmail(), "OTP expired");
            return false; // OTP expired
        }
        if (!u.getOtpCode().equals(otpCode)) {
            operationLogService.logAction("USER_VERIFICATION_FAILED", "USER", u.getId(), u.getEmail(), "Wrong OTP");
            return false; // Invalid OTP
        }
        u.setStatus(UserStatus.ACTIVE); // Set user status to ACTIVE
        u.setOtpCode(null); // Clear OTP code after verification
        u.setOtpExpiry(null); // Clear OTP expiry after verification
        userRepository.save(u); // Save updated user status
        operationLogService.logAction("USER_VERIFICATION_SUCCESSFUL", "USER", u.getId(), u.getEmail(), "User verified with success");
        return true; // OTP verified successfully
    }

    /*
     * Resend OTP to user
     */
    @Override
    public void resendOtp(String email) throws MessagingException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (u.getStatus() == UserStatus.ACTIVE) {
            operationLogService.logAction("USER_RESEND_OTP_FAILED", "USER", u.getId(), u.getEmail(), "User already active");
            throw new IllegalArgumentException("User already active.");
        }
        // Generate new OTP
        String newOtp = String.format("%06d", (int) (Math.random() * 1000000));
        u.setOtpCode(newOtp);
        u.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // Reset OTP expiry to 5 minutes from now
        userRepository.save(u);

        // Send new OTP via email and SMS
        otpService.sendOtp(u, newOtp);
        operationLogService.logAction("USER_RESEND_OTP_FAILED", "USER", u.getId(), u.getEmail(), "User already active");
        String message = "Your new OTP for verification is: " + newOtp;
        // smsService.sendSms(u.getPhoneNumber(), message);
        emailService.sendEmail(email, "OTP Verification", message);
    }

    /**
     * Retrieve a user by ID.
     */
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    /**
     * Fetch all users.
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /*
     * Update an existing user by ID.
     */
    @Override
    public User updateUser(Long id, User updatedUser) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail).orElseThrow( () -> new RuntimeException("Authenticated user not found with email: " + authenticatedEmail));
        
        // Ensure the user exists before updating
        if (authenticatedUser.getId() != id && !authenticatedUser.IsAdmin()){
            operationLogService.logAction("USER_UPDATE_FAILED", "USER", updatedUser.getId(), authenticatedEmail, "User not authorized to update this user");
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Can't update this user"
            );
        }

        User existingUser = getUserById(id);
        // Update only provided fields
        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isBlank()){
            existingUser.setFullName(updatedUser.getFullName());
        }

        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().isBlank()){
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()){
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getFullName()));
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()){
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getStatus() != null){
            existingUser.setStatus(updatedUser.getStatus());
        }

        if (updatedUser.getRoles() != null){
            if (authenticatedUser.getRoles() != UserRole.ADMIN){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update roles");
            }
            existingUser.setRoles(updatedUser.getRoles());
        }
        operationLogService.logAction("USER_UPDATED_SUCCESSFULLY", "USER", updatedUser.getId(), authenticatedEmail, "User successfully updated");
        return userRepository.save(existingUser);
    }
}
