package com.moveo.flight.api.model;

public enum ReservationStatus {
    PENDING,   // Reservation is pending confirmation
    CONFIRMED, // Reservation has been confirmed - paid
    CANCELLED, // Reservation has been cancelled
    CLOSED
}
