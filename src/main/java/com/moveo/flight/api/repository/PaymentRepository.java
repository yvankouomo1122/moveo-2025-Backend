package com.moveo.flight.api.repository;

import com.moveo.flight.api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    Optional<Payment> findByStripePaymentId(String stripePaymentId);
}

