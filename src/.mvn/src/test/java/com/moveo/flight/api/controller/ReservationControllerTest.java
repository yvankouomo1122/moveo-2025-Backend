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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moveo.flight.api.service.ReservationService;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.dto.ReservationDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Arrays;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class ReservationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateReservationEndpoint_Success() throws Exception{
        // Create a mock user object
        User mockUser = new User();
        // Create a mock flight object
        Flight mockFlight = new Flight();
        mockFlight.setPricePerHour(100L);
        // Create a mock reservation object
        Reservation mockReservation = new Reservation();
        // Set necessary fields for the mock objects
        mockReservation.setId(1L);
        mockReservation.setUser(mockUser);
        mockReservation.setFlight(mockFlight);
        mockReservation.setTotalPrice(500L);

        // Mock the service response
        Mockito.when(reservationService.createReservation(eq(1L), eq(2L), any(Reservation.class))).thenReturn(mockReservation);

        // Prepare reservation request payload
        Reservation reservationRequest = new Reservation();
        reservationRequest.setStartDate(java.time.LocalDateTime.now());
        reservationRequest.setEndDate(java.time.LocalDateTime.now().plusHours(5));
        reservationRequest.setFlight(mockFlight);
        reservationRequest.setUser(mockUser);
        reservationRequest.setId(1L);

        // Perform the POST request
        mockMvc.perform(post("/api/reservations")
                .param("userId", "1")
                .param("flightId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(500L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetReservationById_Success() throws Exception {
        // Create a mock reservation object
        Reservation mockReservation = new Reservation();
        mockReservation.setId(1L);
        mockReservation.setTotalPrice(500L);

        // Mock the service response
        Mockito.when(reservationService.getReservationById(1L)).thenReturn(mockReservation);
        Mockito.when(reservationService.toDTO(mockReservation)).thenCallRealMethod();

        // Perform the GET request
        mockMvc.perform(get("/api/reservations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(500L));
    }

    @Test
    void testGetAllReservationsForUser_Success() throws Exception {
        // Create mock user object
        User mockUser = new User();
        mockUser.setId(1L);
        // Create mock reservation objects
        Reservation mockReservation1 = new Reservation();
        mockReservation1.setId(1L);
        mockReservation1.setTotalPrice(500L);
        mockReservation1.setUser(mockUser);
        Reservation mockReservation2 = new Reservation();
        mockReservation2.setId(2L);
        mockReservation2.setTotalPrice(300L);
        mockReservation2.setUser(mockUser);
        // Create mock DTOs
        ReservationDTO dto1 = new ReservationDTO();
        dto1.setId(1L);
        dto1.setTotalPrice(500L);
        ReservationDTO dto2 = new ReservationDTO();
        dto2.setId(2L);
        dto2.setTotalPrice(300L);
        List<ReservationDTO> mockReservations = Arrays.asList(dto1, dto2);

        // Mock the service response
        Mockito.when(reservationService.getReservationsByUser(1L)).thenReturn(mockReservations);
        Mockito.when(reservationService.toDTO(mockReservation1)).thenReturn(dto1);
        Mockito.when(reservationService.toDTO(mockReservation2)).thenReturn(dto2);

        // Perform the GET request
        mockMvc.perform(get("/api/reservations/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].totalPrice").value(500L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].totalPrice").value(300L));
    }

    @Test
    void testCancelReservation_Success() throws Exception {
        // Create a mock reservation object
        Reservation mockReservation = new Reservation();
        mockReservation.setId(1L);
        mockReservation.setTotalPrice(500L);

        // Create a mock DTO object
        ReservationDTO mockDTO = new ReservationDTO();
        mockDTO.setId(1L);
        mockDTO.setTotalPrice(500L);
        mockDTO.setStatus(com.moveo.flight.api.model.ReservationStatus.CANCELLED);

        // Mock the service response
        Mockito.when(reservationService.cancelReservation(1L)).thenReturn(mockDTO);

        // Perform the PUT request
        mockMvc.perform(put("/api/reservations/1/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
