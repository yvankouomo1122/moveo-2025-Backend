package com.moveo.flight.api.services;

import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.moveo.flight.api.service.FlightService;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;
import com.moveo.flight.api.repository.FlightRepository;

public class FlightServiceTest {
    
    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    public FlightServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchFlights_AllFilters() {
        // Arrange
        Flight flight1 = new Flight();
        flight1.setModel("Boeing 737");
        flight1.setStatus(FlightStatus.AVAILABLE);
        flight1.setPricePerHour(200L);

        Flight flight2 = new Flight();
        flight2.setModel("Boeing 737");
        flight2.setStatus(FlightStatus.AVAILABLE);
        flight2.setPricePerHour(350L);

        List<Flight> mockFlights = Arrays.asList(flight1, flight2);

        // Mock the repository method
        when(flightRepository.searchFlights("Boeing 737", FlightStatus.AVAILABLE, 150.0, 300.0))
            .thenReturn(mockFlights);

        // Act
        List<Flight> result = flightService.searchFlights("Boeing 737", "AVAILABLE", 150.0, 300.0);
    
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Boeing 737", result.get(0).getModel());
        assertEquals(FlightStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void testSearchFlights_StatusNull() {
        // Arrange
        Flight flight = new Flight();
        flight.setModel("Airbus A320");
        flight.setStatus(FlightStatus.RESERVED);
        flight.setPricePerHour((long) 300.0);

        when(flightRepository.searchFlights("Airbus A320", null, null, null))
                .thenReturn(List.of(flight));

        // Act
        List<Flight> result = flightService.searchFlights("Airbus A320", null, null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Airbus A320", result.get(0).getModel());
        assertNotNull(result.get(0).getStatus());
    }
}
