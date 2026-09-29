package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.PriceEstimationRequest;
import com.moveo.flight.api.dto.PriceEstimationResponse;
import com.moveo.flight.api.dto.ReservationDTO;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// For @PreAuthorize to work
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * REST Controller for handling reservation requests.
 */
@RestController
@EnableMethodSecurity // Enable method-level security annotations
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Create a new reservation
     * Example request:
     * POST /api/reservations?userId=1&flightId=2&destinationId=1
     * { "startDate":"2025-08-01T10:00:00", "nbPassengers":2, "flightOption":"WITH_PILOT" }
     * 
     * The reservation endDate is calculated automatically from the destination's estimated duration.
     */
    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(
            @RequestParam Long userId,
            @RequestParam Long flightId,
            @RequestParam Long destinationId,
            @RequestBody Reservation reservationRequest
    ) {
        Reservation reservation = reservationService.createReservation(userId, flightId, destinationId, reservationRequest);
        ReservationDTO dto = reservationService.toDTO(reservation);
        return ResponseEntity.ok(dto);
    }

    /*
     * Get a price estimation for a new reservation
     */
    @PostMapping("/estimate-price")
    public ResponseEntity<PriceEstimationResponse> estimatePrice(@RequestBody PriceEstimationRequest request){
        BigDecimal estimated = reservationService.estimateTotalPrice(
            request.getFlightId(),
            request.getStartDate(),
            request.getEndDate(),
            request.getNbPassengers(),
            request.getFlightOption());
        return ResponseEntity.ok(new PriceEstimationResponse(estimated));
    }

    /** Get reservation by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        // Map to safe DTO to avoid lazy-loading issues
        ReservationDTO dto = reservationService.toDTO(reservation);
        return ResponseEntity.ok(dto);
    }

    /** Get all reservations for a user */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getUserReservations(@PathVariable Long userId) {
        List<ReservationDTO> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    /** Cancel a reservation */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long id) {
        ReservationDTO reservations = reservationService.cancelReservation(id);
        return ResponseEntity.ok(reservations);
    }

    /** Close a reservation */
    @PutMapping("/{id}/close")
    public ResponseEntity<ReservationDTO> closeReservation(@PathVariable Long id) {
        ReservationDTO reservations = reservationService.closeReservation(id);
        return ResponseEntity.ok(reservations);
    }

    /** Get all reservations (admin use) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")// Ensure only admins can access this endpoint
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
}
