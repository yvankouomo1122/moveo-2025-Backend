package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.PasswordResetConfirmDTO;
import com.moveo.flight.api.dto.PasswordResetRequestDTO;
import com.moveo.flight.api.dto.SimpleMessageResponse;
import com.moveo.flight.api.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // POST /api/auth/password/request
    @PostMapping("/request")
    public ResponseEntity<SimpleMessageResponse> requestReset(@RequestBody PasswordResetRequestDTO req) {
        return ResponseEntity.ok(passwordResetService.requestReset(req));
    }

    // POST /api/auth/password/confirm
    @PostMapping("/confirm")
    public ResponseEntity<SimpleMessageResponse> confirmReset(@RequestBody PasswordResetConfirmDTO req) {
        return ResponseEntity.ok(passwordResetService.confirmReset(req));
    }
}
