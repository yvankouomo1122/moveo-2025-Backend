package com.moveo.flight.api.controller;

import com.moveo.flight.api.dto.DashboardClientDTO;
import com.moveo.flight.api.dto.DashboardSummaryDTO;
import com.moveo.flight.api.dto.FlightDTO;
import com.moveo.flight.api.service.DashboardService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* Controller for dashboard
*/
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    // GET /api/dashboard/summary
    // Returns dashboard summary data, including paid destination aggregates and the most chosen paid flight.
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(){
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    // GET /api/dashboard/flights
    @GetMapping("/flights")
    public ResponseEntity<List<FlightDTO>> getAllFlights(){
        return ResponseEntity.ok(dashboardService.getAllFlights());
    }

    // GET /api/dashboard/clients
    @GetMapping("/clients")
    public ResponseEntity<List<DashboardClientDTO>> getClients(){
        return ResponseEntity.ok(dashboardService.getClients());
    }
}
