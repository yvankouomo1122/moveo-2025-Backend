package com.moveo.flight.api.scheduler;

import com.moveo.flight.api.repository.BlacklistedTokenRepository;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BlacklistedTokenScheduler {
    private final BlacklistedTokenRepository repository;

    public BlacklistedTokenScheduler(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.repository = blacklistedTokenRepository;
    }

    /**
     * Runs every 1 hour to clear expired blacklisted tokens
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour in ms
    public void clearExpiredTokens() {
        repository.deleteAllExpiredTokens(LocalDateTime.now());
    }
}
