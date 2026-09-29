package com.moveo.flight.api.service;

import com.moveo.flight.api.dto.AuthRequest;
import com.moveo.flight.api.dto.AuthResponse;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserStatus;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.security.JwtUtil;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


/**
 * Handles user authentication logic (login).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    @Autowired
    public AuthService(UserRepository userRepository,
    OperationLogRepository logRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /**
     * Authenticate user and return JWT token.
     */
    public AuthResponse login(AuthRequest request) {
        Optional<User> u = userRepository.findByEmail(request.getEmail());
        if (u.isEmpty()){
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"
            );
        }
        User user = u.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            operationLogService.logAction("LOGIN_FAILED", "User", null, null, "User failed to login with username : " + request.getEmail());
            return new AuthResponse(null, "Invalid credentials");
        }

        // Generate JWT token
        if (user.getStatus() == UserStatus.ACTIVE){
            String token = JwtUtil.generateToken(user.getEmail());
            operationLogService.logAction("LOGIN_SUCCESSFUL", "User", user.getId(), user.getEmail(), "User logged in successfully");
            return new AuthResponse(token, user.getId().toString());
        } else if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            operationLogService.logAction("LOGIN_FAILED", "User", user.getId(), user.getEmail(), "User not verified");
            return new AuthResponse(null, "User is not verified yet. Please verify your email.");
        } else {
            operationLogService.logAction("LOGIN_FAILED", "User", user.getId(), user.getEmail(), "User is deactivated");
            return new AuthResponse(null, "User is disabled. Please contact support.");
        }
    }
}

