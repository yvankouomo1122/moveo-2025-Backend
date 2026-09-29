package com.moveo.flight.api.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.moveo.flight.api.service.StatisticsService;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService){
        this.statisticsService = statisticsService;
    }

    /*
    Trafic (nombre de réservations par période)
    http://localhost:8000/api/admin/statistics/traffic?start=2024-01-01&end=2026-01-01
    */
    @GetMapping("/traffic")
    public ResponseEntity<Map<String, Object>> getReservationCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(statisticsService.getReservationCount(start, end));
    }

    // Utilisateurs actifs
    @GetMapping("/active-users")
    public ResponseEntity<Map<String, Object>> getActiveUsers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(statisticsService.getActiveUsersCount(start, end));
    }

    // Revenus estimés
    @GetMapping("/income")
    public ResponseEntity<Map<String, Object>> getEstimatedRevenues(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(statisticsService.getEstimatedRevenues(start, end));
    }
}
