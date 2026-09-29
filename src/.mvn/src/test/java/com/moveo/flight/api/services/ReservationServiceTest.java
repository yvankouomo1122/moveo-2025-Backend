package com.moveo.flight.api.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.moveo.flight.api.dto.ReservationDTO;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.ReservationStatus;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.service.ReservationService;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

public class ReservationServiceTest {
    private User authenticatedUser;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext SecurityContext;
    @InjectMocks
    private ReservationService reservationService;

    public ReservationServiceTest() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContextHolder
        SecurityContextHolder.setContext(SecurityContext);
        when(SecurityContext.getAuthentication()).thenReturn(authentication);

        authenticatedUser = new User();
        authenticatedUser.setEmail("test@moveo.com");
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(UserRole.ADMIN);
        when(authentication.getName()).thenReturn("test@moveo.com");

    }

    @Test
    void testCreateReservation_Success() {
        // Mock user and flight
        User user = new User();
        user.setId(1L);
        user.setEmail("test@moveo.com");

        Flight  flight = new Flight();
        flight.setId(1L);
        flight.setPricePerHour(100L);

        // Mock reservation
        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDateTime.now());
        reservation.setEndDate(LocalDateTime.now().plusHours(2));

        // Mock repository responses
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(authenticatedUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(reservationRepository.save(reservation)).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the service method
        Reservation createdReservation = reservationService.createReservation(1L, 1L, reservation);

        // Verify the reservation details
        assertNotNull(createdReservation);
        assertEquals(user, createdReservation.getUser());
        assertEquals(flight, createdReservation.getFlight());
        assertEquals(200L, createdReservation.getTotalPrice());
        assertEquals(ReservationStatus.PENDING, createdReservation.getStatus());
    }

    @Test
    void testCreateReservation_UserNotFound() {
        // Mock reservation
        Reservation reservation = new Reservation();

        // Mock repository responses
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(1L, 1L, reservation);
        });
    }

    @Test
    void testCreateReservation_FlightNotFound() {
        // Mock reservation
        Reservation reservation = new Reservation();

        // Mock repository responses
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(1L, 1L, reservation);
        });
    }

    @Test
    void testGetReservation_Success() {
        // Mock reservation
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(LocalDateTime.now());
        reservation.setEndDate(LocalDateTime.now().plusHours(2));
        reservation.setStatus(ReservationStatus.PENDING);

        // Mock repository responses
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Call the service method and expect an exception
        reservationService.getReservationById(1L);
        assertNotNull(reservation);
        assertEquals(1L, reservation.getId());
    }

    @Test
    void testGetReservationById_NotFound() {
        // Mock repository responses
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the service method and expect an exception
        assertThrows(ResponseStatusException.class, () -> {
            reservationService.getReservationById(1L);
        });
    }

    @Test
    void testGetReservationByUserId_Success() {
        // Mock user and reservation
        User user = new User();
        user.setId(1L);
        user.setEmail("test@moveo.com");
        Reservation reservation = new Reservation();
        Reservation reservation2 = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation2.setId(2L);
        reservation2.setUser(user);

        // List of responses
        List<Reservation> reservation_12 = List.of(reservation, reservation2);

        // Mock repository responses
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserId(1L)).thenReturn(reservation_12);

        // Call the service method
        List<ReservationDTO> reservations = reservationService.getReservationsByUser(1L);

        // Verify the results
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertEquals(1L, reservations.get(0).getId());
        assertEquals(2L, reservations.get(1).getId());
    }

    @Test
    void testCancelReservation_Success() {
        // Mock reservation
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUpdatedAt(LocalDateTime.now());

        // Mock repository responses
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        // Call the service method
        ReservationDTO cancelledReservation = reservationService.cancelReservation(1L);

        // Verify the results
        assertNotNull(cancelledReservation);
        assertEquals(ReservationStatus.CANCELLED, cancelledReservation.getStatus());
    }

    @Test
    void testGetAllReservations_Success() {
        // Mock reservations
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setStatus(ReservationStatus.PENDING);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setStatus(ReservationStatus.CONFIRMED);

        List<Reservation> reservations = List.of(reservation1, reservation2);

        // Mock repository responses
        when(reservationRepository.findAll()).thenReturn(reservations);

        // Call the service method
        List<ReservationDTO> allReservations = reservationService.getAllReservations();

        // Verify the results
        assertNotNull(allReservations);
        assertEquals(2, allReservations.size());
    }   
}
