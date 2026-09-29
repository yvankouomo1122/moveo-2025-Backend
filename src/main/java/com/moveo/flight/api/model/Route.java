package com.moveo.flight.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// Generalement utiliser pour les identifiants d'un tableau
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
public class Route {
    /*
     * Destination entity representing destination of a flight
     * Contains startPoint, endPoint, distance, basePrice, estimatedDuration
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-incremented ID

    // Point de depart
    @Enumerated(EnumType.STRING)
    private RoutePoint startPoint;

    // Point d'arrive
    @Enumerated(EnumType.STRING)
    private RoutePoint endPoint;

    /*
     * Distance du trajet startpoint-endpoint (in Km)
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal distance;

    /*
     * Prix (en Euro)
     */
    @Column(nullable = false)
    private  Long basePrice = 500L;

    /*
     * Duree estimee du vol (en minutes)
     */
    @Column(nullable = false)
    private Integer estimatedDuration;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Date de creation de la destination

    @Column(name="updated_at")
    private LocalDateTime updatedAt; // Date de modification de la destination

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
     * Getters et Setters
     */
    public Long getId(){
        return this.id;
    }

    public RoutePoint getStartPoint(){
        return this.startPoint;
    }

    public void setStartPoint(RoutePoint startPoint){
        this.startPoint = startPoint;
    }

    public RoutePoint getEndPoint(){
        return this.endPoint;
    }

    public void setEndPoint(RoutePoint endPoint){
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
      public void setBasePrice(Long basePrice){
        this.basePrice = basePrice;
    }

    public Integer getEstimatedDuration(){
        return this.estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration){
        this.estimatedDuration = estimatedDuration;
    }
}
