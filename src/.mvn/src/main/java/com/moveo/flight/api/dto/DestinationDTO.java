package com.moveo.flight.api.dto;

import java.math.BigDecimal;

public class DestinationDTO {
    private Long id;
    private String startPoint;
    private String endPoint;
    private BigDecimal distance;
    private Integer estimatedDuration;
    private Long basePrice;

    public DestinationDTO(Long id, String startPoint, String endPoint, BigDecimal distance, Integer estimatedDuration, Long basePrice){
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
    public String getStartPoint(){return this.startPoint;}
    public String getEndPoint(){return this.endPoint;}
    public BigDecimal getDistance(){return this.distance;}
    public Integer getEstimatedDuration(){return this.estimatedDuration;}
    public Long getBasePrice(){return this.basePrice;}
}
