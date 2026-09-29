package com.moveo.flight.api.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moveo.flight.api.model.BlacklistedToken;

import jakarta.transaction.Transactional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    
    // Delete all tokens expired before now
    @Transactional
    @Modifying
    @Query("DELETE FROM BlacklistedToken t WHERE t.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);
    boolean existsByToken(String token);
}
