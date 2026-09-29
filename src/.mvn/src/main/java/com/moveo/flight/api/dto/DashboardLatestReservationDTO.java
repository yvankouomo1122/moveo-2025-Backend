package com.moveo.flight.api.dto;

import java.time.LocalDateTime;

public class DashboardLatestReservationDTO {
    private String clientName;
    private String destination;
    private LocalDateTime startDate;
    private Integer nbPassengers;
    private String status;

    public DashboardLatestReservationDTO() {}

    public DashboardLatestReservationDTO(String clientName, String destination,
        LocalDateTime startDate, Integer nbPassengers, String status
    ){
        this.clientName = clientName;
        this.destination = destination;
        this.startDate = startDate;
        this.nbPassengers = nbPassengers;
        this.status = status;
    }

    public String getClientName(){
        return clientName;
    }

    public String getDesintation(){
        return destination;
    }

    public LocalDateTime getStartDate(){
        return startDate;
    }

    public Integer getNbPassengers(){
        return nbPassengers;
    }

    public String getStatus(){
        return status;
    }

    public void setClientName(String clientName){
        this.clientName = clientName;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }
    
    public void setStartDate(LocalDateTime startDate){
        this.startDate = startDate;
    }

    public void setNbPassengers(Integer nbPassengers){
        this.nbPassengers = nbPassengers;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
