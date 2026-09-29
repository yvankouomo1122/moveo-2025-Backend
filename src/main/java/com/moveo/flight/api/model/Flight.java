package com.moveo.flight.api.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
public class Flight {
    /**
     * Flight entity representing aircraft details.
     * - Contains flight model, capacity, status, price, images
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String model; // Model de l'avion (e.g. Cessna 172)
    
    private Integer capacity; // Capacite de l'avion

    // Flight status enum
    @Enumerated(EnumType.STRING)
    private FlightStatus status = FlightStatus.AVAILABLE; // statut de l'avion par defaut est AVAILABLE
    
    private BigDecimal pricePerHour; // Prix par heur d'un avion.

    @Column(columnDefinition = "TEXT")
    private String images; // JSON array of image URLs

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Created timestamp
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // Updated timestamp

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    private List<Reservation> reservations; // Un avion peut avoir plusieurs reservations

    // Auto-fill timestamps before saving
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    // Auto-update timestamp before updating
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructeur 
    public Flight() {};

    public Flight(Long id, String model, Integer capacity, FlightStatus status, BigDecimal pricePerHour, List<String> images){
        this.id = id;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
        this.pricePerHour = pricePerHour;
        this.images = images != null ? images.toString() : null; // Convert List<String> to JSON-like string
    }

    // Get id model, status, price, images, capacity
    public Long getId() {
        return id;
    }
    public String getModel() {
        return model;
    }
    public FlightStatus getStatus() {
        return status;
    }
    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }
    public BigDecimal perPricePerMinute(){
        if (pricePerHour == null){
            return BigDecimal.ZERO;
        }
        return pricePerHour.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
    public String getImages() {
        return images;
    }
    public Integer getCapacity() {
        return capacity;
    }
    // Set id, model, status, price, images, capacity
    public void setId(Long id) {
        this.id = id;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setStatus(FlightStatus status) {
        this.status = status;
    }
    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
    public void setImages(String images) {
        this.images = images;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

}
