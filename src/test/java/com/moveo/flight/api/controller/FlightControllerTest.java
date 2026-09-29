package com.moveo.flight.api.controller;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moveo.flight.api.service.FlightService;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;
import com.moveo.flight.api.repository.FlightRepository;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class FlightControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private FlightRepository flightRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllFlightsEndpoint_Success() throws Exception {
        // Mock the flights list
        Flight flight1 = new Flight(1L, "Boeing 737", 180, FlightStatus.AVAILABLE, 5000L, Arrays.asList("image1.jpg", "image2.jpg"));
        Flight flight2 = new Flight(2L, "Airbus A320", 150, FlightStatus.RESERVED, 4500L, Arrays.asList("image3.jpg"));
        List<Flight> mockFlights = Arrays.asList(flight1, flight2);

        // Mock the service
        Mockito.when(flightRepository.findAll()).thenReturn(mockFlights);

        // Perform the get request
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].model").value("Boeing 737"))
                .andExpect(jsonPath("$[1].pricePerHour").value(4500L));
    }

    @Test
    void testCreateFlightEndpoint_Success() throws Exception {
        // Mock the flight
        Flight flight = new Flight(1L, "Boeing 737", 180, FlightStatus.AVAILABLE, 5000L, Arrays.asList("image1.jpg", "image2.jpg"));
        
        // Mock the service
        Mockito.when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        // Perform the post request
        mockMvc.perform(post("/api/flights")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(flight)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.model").value("Boeing 737"));
    }

    @Test
    void testGetFlightByIdEndpoint_Success() throws Exception {
        // Mock the flight
        Flight flight = new Flight(1L, "Boeing 737", 180, FlightStatus.AVAILABLE, 5000L, Arrays.asList("image1.jpg", "image2.jpg"));
        
        // Mock the service
        Mockito.when(flightRepository.findById(eq(1L))).thenReturn(Optional.of(flight));

        // Perform the get request
        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Boeing 737"));
    }

    @Test
    void testUpdateFlightEndpoint_Success() throws Exception{
        // Mock the request payload
        Flight reqFlight = new Flight(1L, "Boeing 737", 180, FlightStatus.AVAILABLE, 5000L, Arrays.asList("image1.jpg", "image2.jpg"));

        // Mock the existing flight
        Flight flight = new Flight(2L, "Airbus A320", 150, FlightStatus.RESERVED, 4500L, Arrays.asList("image3.jpg"));

        // Mock the service
        Mockito.when(flightRepository.findById(eq(1L))).thenReturn(Optional.of(flight));
        Mockito.when(flightRepository.save(any(Flight.class))).thenReturn(reqFlight);

        // Peform the put request
        mockMvc.perform(put("/api/flights/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqFlight)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.model").value("Boeing 737"));
    }

    @Test
    void testDeleteFlightEndpoint_Success() throws Exception{
        // Mock the flight
        Flight flight = new Flight(1L, "Boeing 737", 180, FlightStatus.AVAILABLE, 5000L, Arrays.asList("image1.jpg", "image2.jpg"));

        // Mock the service
        Mockito.when(flightRepository.findById(eq(1L))).thenReturn(Optional.of(flight));

        // Peform the put request
        mockMvc.perform(delete("/api/flights/1"))
                    .andExpect(status().isNoContent());

        Mockito.verify(flightRepository).delete(flight);
    }

    @Test
    void testDeleteFlight_NotFound() throws Exception {
        // when
        Mockito.when(flightRepository.findById(eq(999L))).thenReturn(Optional.empty());

        // then
        mockMvc.perform(delete("/api/flights/999"))
                .andExpect(status().isNotFound());

        Mockito.verify(flightRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    void testSearchFlightEndpoint_success() throws Exception {
        // Mock flight1 object
        Flight flight1 = new Flight();
        flight1.setId(1L);
        flight1.setModel("Boeing 747");
        flight1.setStatus(FlightStatus.AVAILABLE);
        flight1.setPricePerHour(5000L);

        Flight flight2 = new Flight();
        flight2.setId(2L);
        flight2.setModel("Airbus A320");
        flight2.setStatus(FlightStatus.AVAILABLE);
        flight2.setPricePerHour(3000L);

        List<Flight> flights = Arrays.asList(flight1, flight2);

        Mockito.when(flightService.searchFlights(eq("Boeing"), eq("AVAILABLE"), eq(2000.0), eq(6000.0)))
                .thenReturn(flights);

        // when & then
        mockMvc.perform(get("/api/flights/search")
                        .param("model", "Boeing")
                        .param("status", "AVAILABLE")
                        .param("minPrice", "2000")
                        .param("maxPrice", "6000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Boeing 747"))
                .andExpect(jsonPath("$[1].model").value("Airbus A320"));
    }
}
