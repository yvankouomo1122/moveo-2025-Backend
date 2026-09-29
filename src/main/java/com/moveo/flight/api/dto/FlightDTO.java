package com.moveo.flight.api.dto;
import java.math.BigDecimal;

import com.moveo.flight.api.model.FlightStatus;
public class FlightDTO {
    private Long id;
    private String model;
    private Integer capacity;
    private FlightStatus status;
    private BigDecimal pricePerHour;
    private String images;

    public FlightDTO() {}

    public FlightDTO(Long id, String model, Integer capacity, FlightStatus status,
        BigDecimal pricePerHour, String images
    ){
        this.id = id;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
        this.pricePerHour = pricePerHour;
        this.images = images;
    }

    public Long getId(){
        return id;
    }

    public String getModel(){
        return model;
    }

    public Integer getCapacity(){
        return capacity;
    }

    public FlightStatus getFlightStatus(){
        return status;
    }

    public BigDecimal getPricePerHour(){
        return pricePerHour;
    }

    public String getImages(){
        return images;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setModel(String model){
        this.model = model;
    }

    public void setCapacity(Integer capacity){
        this.capacity = capacity;
    }

    public void setFlightStatus(FlightStatus status){
        this.status = status;
    }

    public void setPricePerHour(BigDecimal pricePerHour){
        this.pricePerHour = pricePerHour;
    }

    public void setImages(String images){
        this.images = images;
    }
}