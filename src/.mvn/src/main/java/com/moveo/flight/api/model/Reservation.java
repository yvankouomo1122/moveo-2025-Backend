package com.moveo.flight.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    private Long id;
    
    /**
     * The user who made the reservation.
     * Many reservations can belong to one user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The flight associated with this reservation.
     * Many reservations can belong to one flight.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    /**
     * Reservation start and end date.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Reservation status (PENDING, CONFIRMED, CANCELED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    /**
     * Total price for this reservation. The currency is by default EURO.
     */
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(nullable = false)
    private Integer nbPassengers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightOption flightOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Destination destination;
    
    /**
     * Audit fields for creation and update tracking.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * One-to-one relation with payment.
     * A reservation has one unique payment.
     */
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) {
        if (this.endDate != null && !this.endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) {
        if (endDate == null) {
            this.endDate = null;
            return;
        }
        if (this.startDate != null && !endDate.isAfter(this.startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
        this.endDate = endDate;
    }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public Long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Long totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Integer getNbPassengers() { return nbPassengers;}
    public void setNbPassengers(Integer nbPassengers) {         
    if (nbPassengers != null && nbPassengers > 5) {
        throw new IllegalArgumentException("Number of passengers cannot be greater than 5");
    }
    this.nbPassengers = nbPassengers;}

    public FlightOption getFlightOption() { return flightOption;}
    public void setFlightOption(FlightOption flightOption) { this.flightOption = flightOption;}
}
