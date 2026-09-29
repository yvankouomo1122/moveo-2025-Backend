package com.moveo.flight.api.service;

import com.moveo.flight.api.dto.PaymentDTO;
import com.moveo.flight.api.model.*;
import com.moveo.flight.api.repository.PaymentRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.moveo.flight.api.repository.NotificationRepository;
import com.moveo.flight.api.repository.OperationLogRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository,
                          NotificationRepository notificationRepository,
                          OperationLogRepository logRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    @Transactional
    public PaymentDTO processPayment(Long reservationId, NotificationType notificationType) throws StripeException{
        // Find reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reservation already paid & confirmed!");
        }

        // Create Stripe PaymentIntent
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(reservation.getTotalPrice())
                        .setCurrency("eur")
                        .setDescription("Payment for reservation with ID #" + reservation.getId() + " by user " + reservation.getUser().getEmail())
                        .putMetadata("notificationType", notificationType.name()) // Store EMAIL or SMS type
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        // Create a payment record
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalPrice());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setStripePaymentId(paymentIntent.getId());
        paymentRepository.save(payment);
        
        PaymentDTO dto = new PaymentDTO(payment.getId(),
            payment.getAmount(),
            payment.getPaymentStatus().name(),
            payment.getStripePaymentId(),
            payment.getCreatedAt(),
            payment.getReservation().getId(),
            payment.getReservation().getStatus().name(),
            payment.getReservation().getUser().getId(),
            payment.getReservation().getUser().getEmail()
        );

        // Attach Stripe client secret to DTO for frontend to confirm
        dto.setStripeClientSecret(paymentIntent.getClientSecret()); // Frontend will confirm the payment with Stripe using the client_secret. Stripe webhook will update the payment status later.
        return dto;
    }

    public Payment getPaymentByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for reservation"));
    }

    public void handleSuccessfulPayment(String stripePaymentId, String notificationType) {
        paymentRepository.findByStripePaymentId(stripePaymentId).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            // Update reservation status
            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            // Create notification for confirmed reservation
            Notification notif = new Notification();
            notif.setUser(reservation.getUser());
            notif.setMessage("Your reservation with ID #" + reservation.getId() + " has been confirmed and paid!");
            notif.setStatus(NotificationStatus.PENDING);
            notif.setType(notificationType);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();
            operationLogService.logAction("PAYMENT_SUCCESSFUL", "Payment", reservation.getId(), authenticatedEmail, "Your reservation with ID #" + reservation.getId() + " has been confirmed and paid!");
            notificationRepository.save(notif);
        });
    }

    public void handleFailedPayment(String stripePaymentId) {
        paymentRepository.findByStripePaymentId(stripePaymentId).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // Optionally notify the user about failure
            Reservation reservation = payment.getReservation();
            Notification notif = new Notification();
            notif.setUser(reservation.getUser());
            notif.setMessage("Payment failed for reservation #" + reservation.getId());
            notif.setStatus(NotificationStatus.PENDING);
            notif.setType(NotificationType.EMAIL);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedEmail = authentication.getName();
            operationLogService.logAction("PAYMENT_SUCCESSFUL", "Payment", reservation.getId(), authenticatedEmail, "Payment failed for reservation #" + reservation.getId());
            notificationRepository.save(notif);
        });
    }
}
