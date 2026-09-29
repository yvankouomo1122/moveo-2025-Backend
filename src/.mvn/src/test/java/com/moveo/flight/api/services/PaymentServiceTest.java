package com.moveo.flight.api.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import com.moveo.flight.api.dto.PaymentDTO;
import com.moveo.flight.api.model.NotificationType;
import com.moveo.flight.api.model.Payment;
import com.moveo.flight.api.model.PaymentStatus;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.ReservationStatus;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.Notification;
import com.moveo.flight.api.repository.NotificationRepository;
import com.moveo.flight.api.repository.PaymentRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.service.PaymentService;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

public class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationRepository notificationRepository;
    
    @InjectMocks
    private PaymentService paymentService;

    public PaymentServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPayment_Success() throws StripeException{
        // Mock reservation and user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUser(user);
        reservation.setTotalPrice(200L);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(100L))
            .thenReturn(Optional.of(reservation));

        // Mock Stripe PaymentIntent creation
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(mockIntent.getId()).thenReturn("pi_12345");
        when(mockIntent.getClientSecret()).thenReturn("secret_123");

        // Mock the PaymentIntent creation logic
        MockedStatic<PaymentIntent> paymentIntentMockedStatic = mockStatic(PaymentIntent.class);
        paymentIntentMockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
            .thenReturn(mockIntent);

        PaymentDTO dto = paymentService.processPayment(100L, NotificationType.EMAIL);

        assertNotNull(dto);
        assertEquals("pi_12345", dto.getStripePaymentId());
        assertEquals("secret_123", dto.getStripeClientSecret());
        assertEquals(PaymentStatus.PENDING.name(), dto.getPaymentStatus());
    }

    @Test
    void testProcessPayment_ReservationNotFound(){
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> paymentService.processPayment(999L, NotificationType.EMAIL));
        assertEquals("Reservation not found", ex.getMessage());
    }

    @Test
    void testProcessPayment_AlreadyConfirmed(){
        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> paymentService.processPayment(100L, NotificationType.EMAIL));
        
        assertEquals("Reservation already paid & confirmed!", ex.getMessage());
    }

    @Test
    void testProcessPayment_getPaymentByReservation_Success() {
        // Mock Reservation and Payment
        Reservation reservation = new Reservation();
        reservation.setId(100L);
        
        Payment payment = new Payment();
        payment.setId(100L);
        payment.setReservation(reservation);

        // Mock the repository to return the payment
        when(paymentRepository.findByReservationId(100L)).thenReturn(Optional.of(payment));
        Payment foundPayment = paymentService.getPaymentByReservation(100L);
        assertNotNull(foundPayment);
        assertEquals(100L, foundPayment.getId());
    }

    @Test
    void testHandleSuccessfulPayment_Success() {
        // Mock User, Reservation, and Payment
        User user = new User();
        user.setId(1L);
        user.setEmail("test@moveo.com");

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(50L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setStripePaymentId("pi_123");

        // Mock repositories
        when(paymentRepository.findByStripePaymentId("pi_123")).thenReturn(Optional.of(payment));
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0)); // Mock notification save

        // Call the method
        paymentService.handleSuccessfulPayment("pi_123", NotificationType.EMAIL.name());

        // Verify payment status update
        assert(payment.getPaymentStatus() == PaymentStatus.PAID);
        assert(reservation.getStatus() == ReservationStatus.CONFIRMED);
    }

    @Test
    void testHandleFailedPayment_Success() {
        // Mock User, Reservation, and Payment
        User user = new User();
        user.setId(1L);
        user.setEmail("test@moveo.com");

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(50L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setStripePaymentId("pi_456");

        // Mock repositories
        when(paymentRepository.findByStripePaymentId("pi_456")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0)); // Mock notification save

        // Call the method
        paymentService.handleFailedPayment("pi_456");

        // Verify payment status update
        assert(payment.getPaymentStatus() == PaymentStatus.FAILED);
    }
}
