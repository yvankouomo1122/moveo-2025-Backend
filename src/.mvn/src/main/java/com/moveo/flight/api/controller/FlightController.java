package com.moveo.flight.api.controller;

import java.rmi.server.Operation;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.service.FlightService;
import com.moveo.flight.api.service.OperationLogService;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
    @Autowired
    private FlightRepository flightRepository;
    private final FlightService flightService;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public FlightController(FlightService flightService,
        OperationLogRepository logRepository) {
        this.flightService = flightService;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /*
     * Get all flights
     */
    @GetMapping
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    /*
     * Search flights by criteria
     */
    @PostMapping
    public Flight createFlight(@RequestBody Flight flight) {
        Flight saved = flightRepository.save(flight);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("CREATE_FLIGHT", "Flight", saved.getId(), authenticatedEmail, "Created flight");
        return saved;
    }

    /*
     * Get flight by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        return flightRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * Update specific flight by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long id, @RequestBody Flight updatedFlight) {
        return ResponseEntity.ok(flightService.updateFlight(id, updatedFlight));
    }

    /*
     * Delete flight by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        return flightRepository.findById(id).map(flight -> {
                    flightRepository.delete(flight);
                    operationLogService.logAction("DELETE_FLIGHT", "Flight", flight.getId(), authenticatedEmail, "Deleted flight");
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint de recherche de vols
     * Ex: /api/flights/search?model=Boeing&status=AVAILABLE&minPrice=2000&maxPrice=5000
     */
    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        List<Flight> results = flightService.searchFlights(model, status, minPrice, maxPrice);
        return ResponseEntity.ok(results);
    }
}
