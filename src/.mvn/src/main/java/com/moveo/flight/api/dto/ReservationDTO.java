package com.moveo.flight.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.moveo.flight.api.model.ReservationStatus;

/**
 * DTO for safely returning Reservation info without causing infinite loops
 * or lazy-loading proxy issues.
 */
public class ReservationDTO {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long totalPrice;
    private ReservationStatus status;

    // Minimal user info (avoid deep nesting)
    private Long userId;
    private String userEmail;

    // Minimal flight info
    private Long flightId;
    private String flightModel;

    // Minimal Destination info
    private String startPoint;
    private String endPoint;
    private BigDecimal distance;
    private Integer duration;
    
    // Empty constructor for Jackson
    public ReservationDTO() {}

    // Full constructor for easy mapping
    public ReservationDTO(Long id, LocalDateTime startDate, LocalDateTime endDate,
                          Long totalPrice, ReservationStatus status,
                          Long userId, String userEmail,
                          Long flightId, String flightModel,
                          String startPoint, String endPoint, BigDecimal distance, Integer duration) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.userId = userId;
        this.userEmail = userEmail;
        this.flightId = flightId;
        this.flightModel = flightModel;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.duration = duration;
    }

    // Getters (needed for JSON serialization)
    public Long getId() { return id; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Long getTotalPrice() { return totalPrice; }
    public ReservationStatus getStatus() { return status; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public Long getFlightId() { return flightId; }
    public String getFlightModel() { return flightModel; }
    public String getStartPoint() { return startPoint; }
    public String getEndPoint() { return endPoint; }
    public BigDecimal getDistance() { return distance; }
    public Integer getDuration() { return duration; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public void setTotalPrice(Long totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public void setFlightModel(String flightModel) { this.flightModel = flightModel; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }
    public void setDistance(BigDecimal distance) { this.distance = distance; }
    public void setDuration(Integer duration) { this.duration = duration; }
}

