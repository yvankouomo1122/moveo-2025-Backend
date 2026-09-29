package com.moveo.flight.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.repository.ReservationRepository;

@Service
public class StatisticsService {
    private final ReservationRepository reservationRepository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public StatisticsService(ReservationRepository reservationRepository,
        OperationLogRepository logRepository){
        this.reservationRepository = reservationRepository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }
    // Count reservations in period (traffic)
    public Map<String, Object> getReservationCount(LocalDate start, LocalDate end) {
        long count = reservationRepository.countByCreatedAtBetween(start.atStartOfDay(), end.atTime(23,59,59));
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("traffic", count);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("QUERIED_RESERVATION", "RESERVATION", null, authenticatedEmail, "Queried reservations in a period (traffic) from " + start + " to " + end);
        return result;
    }

    // Active users (users who made reservations in the period)
    public Map<String, Object> getActiveUsersCount(LocalDate start, LocalDate end) {
        long count = reservationRepository.countDistinctUsersByCreatedAtBetween(start.atStartOfDay(), end.atTime(23,59,59));
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("activeUsers", count);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("QUERIED_RESERVATION", "RESERVATION", null, authenticatedEmail, "Queried active users in a period from " + start + " to " + end);
        return result;
    }

    // Estimated revenues (based on reservation pricing)
    public Map<String, Object> getEstimatedRevenues(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = (end != null) ? end.atTime(23,59,59) : null;
        BigDecimal totalRevenue = reservationRepository.sumEstimatedRevenueBetween(startDateTime, endDateTime);
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("QUERIED_RESERVATION", "RESERVATION", null, authenticatedEmail, "Queried estimated income for the period " + start + " to " + end);
        return result;
    }
}
