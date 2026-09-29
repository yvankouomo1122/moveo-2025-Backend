package com.moveo.flight.api.service;

import com.moveo.flight.api.dto.PasswordResetConfirmDTO;
import com.moveo.flight.api.dto.PasswordResetRequestDTO;
import com.moveo.flight.api.dto.SimpleMessageResponse;
import com.moveo.flight.api.model.PasswordResetToken;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.repository.PasswordResetTokenRepository;
import com.moveo.flight.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PasswordResetService(UserRepository userRepository,
                               PasswordResetTokenRepository tokenRepository,
                               EmailService emailService,
                               SmsService smsService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public SimpleMessageResponse requestReset(PasswordResetRequestDTO req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String token = UUID.randomUUID().toString().replace("-", "");

        PasswordResetToken reset = new PasswordResetToken();
        reset.setUser(user);
        reset.setToken(token);
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        reset.setUsed(false);
        tokenRepository.save(reset);

        String message = "Moveo Password Reset Token: " + token + " (valid 15 minutes)";

        try {
            emailService.sendEmail(user.getEmail(), "Moveo Password Reset", message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send reset token");
        }

        return new SimpleMessageResponse(true, "Reset token sent successfully");
    }

    @Transactional
    public SimpleMessageResponse confirmReset(PasswordResetConfirmDTO req) {
        if (req.getToken() == null || req.getToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }
        if (req.getNewPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        PasswordResetToken reset = tokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token"));

        if (reset.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token already used");
        }
        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        reset.setUsed(true);
        tokenRepository.save(reset);

        return new SimpleMessageResponse(true, "Password updated successfully");
    }
}
