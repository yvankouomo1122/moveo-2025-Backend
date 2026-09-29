package com.moveo.flight.api.dto;

import java.time.LocalDateTime;

public class PaymentDTO {
    private Long id;
    private Long amount;
    private String paymentStatus;
    private String stripePaymentId;
    private LocalDateTime createdAt;
    private Long reservationId;
    private String reservationStatus;
    private Long userId;
    private String userEmail;
    private String stripeClientSecret;

    // Constructor
    public PaymentDTO(Long id, Long amount, String paymentStatus, String stripePaymentId,
                      LocalDateTime createdAt,
                      Long reservationId, String reservationStatus,
                      Long userId, String userEmail) {
        this.id = id;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.stripePaymentId = stripePaymentId;
        this.createdAt = createdAt;
        this.reservationId = reservationId;
        this.reservationStatus = reservationStatus;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAmount() { return amount; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getStripePaymentId() { return stripePaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getReservationId() { return reservationId; }
    public String getReservationStatus() { return reservationStatus; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getStripeClientSecret() { return stripeClientSecret; }
    
    // Setters
    public void setStripeClientSecret(String clientSecret) { this.stripeClientSecret = clientSecret; }
}

