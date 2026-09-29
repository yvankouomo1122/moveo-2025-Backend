package com.moveo.flight.api.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public FlightService(FlightRepository flightRepository,
        OperationLogRepository logRepository,
        OperationLogService operationLogService) {
        this.flightRepository = flightRepository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /**
     * Flight search by criteria (model, status, min/max price)
     */
    public List<Flight> searchFlights(String model, String status, Double minPrice, Double maxPrice) {
        FlightStatus flightStatus = null;
        if (status != null) {
            flightStatus = FlightStatus.valueOf(status.toUpperCase());
        }
        return flightRepository.searchFlights(model, flightStatus, minPrice, maxPrice);
    }

    /*
     * Update a flight
     */
    public Flight updateFlight(Long id, Flight updated){
        Flight existing = flightRepository.findById(id).orElseThrow(() -> new RuntimeException("Flight not found with id " + id));
        if (updated.getModel() != null && !updated.getModel().isBlank()){
            existing.setModel(updated.getModel());
        }
        if (updated.getCapacity() != null){
            existing.setCapacity(updated.getCapacity());
        }
        if (updated.getPricePerHour() != null){
            existing.setPricePerHour(updated.getPricePerHour());
        }
        if (updated.getStatus() != null){
            existing.setStatus(updated.getStatus());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        Flight saved = flightRepository.save(existing);
        operationLogService.logAction("UPDATE_FLIGHT", "Flight", saved.getId(), authenticatedEmail, "Updated flight");
        return saved;
    }
}

