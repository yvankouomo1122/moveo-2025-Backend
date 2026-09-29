package com.moveo.flight.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"startPoint", "endPoint"})
)
public class Destination {
    /*
     * Destination entity representing destination of a flight
     * Contains startPoint, endPoint, distance, basePrice, estimatedDuration
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-incremented ID

    @Column(nullable = false, length = 100)
    private String startPoint;

    @Column(nullable = false, length = 100)
    private String endPoint;

    /*
     * Destination (in Km)
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal distance;

    /*
     * Price (in Euro)
     */
    @Column(nullable = false)
    private final Long basePrice = 500L;

    /*
     * Estimated duration (in minutes)
     */
    @Column(nullable = false)
    private Integer estimatedDuration;
    
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Created timestamp
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt; // Updated timestamp

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
    
    /*
     * Getters and Setters
     */
    public Long getId(){
        return this.id;
    }
    
    public String getStartPoint(){
        return this.startPoint;
    }

    public void setStartPoint(String startPoint){
        this.startPoint = startPoint;
    }

    public String getEndPoint(){
        return this.endPoint;
    }

    public void setEndPoint(String endPoint){
        this.endPoint = endPoint;
    }

    public BigDecimal getDistance(){
        return this.distance;
    }

    public void setDistance(BigDecimal distance){
        this.distance = distance;
    }

    public Long getBasePrice(){
        return this.basePrice;
    }

    public Integer getEstimatedDuration(){
        return this.estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration){
        this.estimatedDuration = estimatedDuration;
    }
}
