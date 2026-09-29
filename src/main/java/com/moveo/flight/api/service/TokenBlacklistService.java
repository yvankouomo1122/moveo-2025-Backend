package com.moveo.flight.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.moveo.flight.api.model.BlacklistedToken;
import com.moveo.flight.api.repository.BlacklistedTokenRepository;

@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    } 

    public void blacklistToken(String token, LocalDateTime expiryDate) {
        repository.save(new BlacklistedToken(token, expiryDate));
    }

    public boolean isTokenBlacklisted(String token) {
        return repository.existsByToken(token);
    }
}

