package com.moveo.flight.api.dto;

import com.moveo.flight.api.model.FlightStatus;

public class DashboardMostChosenFlightDTO {
    private Long id;
    private String model;
    private Integer capacity;
    private FlightStatus status;
    private Long pricePerHour;
    private String images;
    private Long flightCount;

    public DashboardMostChosenFlightDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Long getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Long pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Long getFlightCount() {
        return flightCount;
    }

    public void setFlightCount(Long flightCount) {
        this.flightCount = flightCount;
    }
}
