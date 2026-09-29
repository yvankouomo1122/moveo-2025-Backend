package com.moveo.flight.api.dto;

import java.time.LocalDateTime;

import com.moveo.flight.api.model.FlightOption;

public class PriceEstimationRequest {
    private Long flightId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int nbPassengers;
    private FlightOption flightOption;

    public Long getFlightId(){ return this.flightId;}
    public LocalDateTime getStartDate() { return this.startDate; }
    public LocalDateTime getEndDate() { return this.endDate; }
    public int getNbPassengers() { return this.nbPassengers; }
    public FlightOption getFlightOption() { return this.flightOption; }
}
