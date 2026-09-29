package com.moveo.flight.api.dto;

public class DashboardRouteDTO {
    private String startPoint;
    private String endPoint;
    private String label;
    private Long flightCount;

    public DashboardRouteDTO() {}

    public DashboardRouteDTO(String startPoint, String endPoint){
        this(startPoint, endPoint, null);
    }

    public DashboardRouteDTO(String startPoint, String endPoint, Long flightCount){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.label = startPoint + " - " + endPoint;
        this.flightCount = flightCount;
    }

    public String getStartPoint(){
        return startPoint;
    }

    public String getEndPoint(){
        return endPoint;
    }

    public String getLable(){
        return label;
    }

    public Long getFlightCount(){
        return flightCount;
    }

    public void setStartPoint(String startPoint){
        this.startPoint = startPoint;
    }

    public void setEndPoint(String endPoint){
        this.endPoint = endPoint;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setFlightCount(Long flightCount){
        this.flightCount = flightCount;
    }
}
