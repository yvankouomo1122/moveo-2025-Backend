package com.moveo.flight.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moveo.flight.api.service.PaymentService;
import com.moveo.flight.api.model.PaymentStatus;
import com.moveo.flight.api.model.NotificationType;
import com.moveo.flight.api.model.ReservationStatus;
import com.moveo.flight.api.model.Payment;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.dto.PaymentDTO;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class PaymentControllerTest {
    private PaymentDTO paymentDTO;
    private Payment payment;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        // Mock reservation object
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Mock payment object
        payment = new Payment();
        payment.setAmount(3000L);
        payment.setId(42L);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setReservation(reservation);

        // Mock paymentDTO
        paymentDTO = new PaymentDTO(42L, 3000L, PaymentStatus.PAID.name(), "123", payment.getCreatedAt(), 1L, ReservationStatus.CONFIRMED.name(), 1L, "test@moveo.com");
    }

    @Test
    void testPayForReservationEndpoint_Success() throws Exception {
        // Mock the service
        Mockito.when(paymentService.processPayment(eq(1L), eq(NotificationType.EMAIL))).thenReturn(paymentDTO);

        // Perform the post request
        mockMvc.perform(post("/api/payments/1")
                        .param("notificationType", "EMAIL")
                        .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.amount").value(3000))
                    .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                    .andExpect(jsonPath("$.reservationId").value(1))
                    .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"));
    }
    
    @Test
    void testGetPaymentDetailsFromReservationEndpoint_Success() throws Exception {
        // Mock the service
        Mockito.when(paymentService.getPaymentByReservation(eq(1L))).thenReturn(payment);

        // Perform the get request
        mockMvc.perform(get("/api/payments/reservation/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.amount").value(3000))
                        .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                        .andExpect(jsonPath("$.id").value(42));
    }
}
