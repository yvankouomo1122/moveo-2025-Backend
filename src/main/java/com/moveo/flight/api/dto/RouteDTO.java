package com.moveo.flight.api.dto;

import java.math.BigDecimal;

import com.moveo.flight.api.model.RoutePoint;

public class RouteDTO {
    private Long id;
    private RoutePoint startPoint;
    private RoutePoint endPoint;
    private BigDecimal distance;
    private Integer estimatedDuration;
    private Long basePrice;

    public RouteDTO(Long id, RoutePoint startPoint, RoutePoint endPoint, BigDecimal distance, Integer estimatedDuration, Long basePrice){
        this.id = id;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.estimatedDuration = estimatedDuration;
        this.basePrice = basePrice;
    }

    /*
     * Getters and setters are important for the functioning of a DTO
     */
    public Long getId(){return this.id;}
    public RoutePoint getStartPoint(){return this.startPoint;}
    public RoutePoint getEndPoint(){return this.endPoint;}
    public BigDecimal getDistance(){return this.distance;}
    public Integer getEstimatedDuration(){return this.estimatedDuration;}
    public Long getBasePrice(){return this.basePrice;}
}
