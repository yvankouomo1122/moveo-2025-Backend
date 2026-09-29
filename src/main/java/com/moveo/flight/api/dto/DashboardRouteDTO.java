package com.moveo.flight.api.dto;

import com.moveo.flight.api.model.RoutePoint;

public class DashboardRouteDTO {
    private RoutePoint startPoint;
    private RoutePoint endPoint;
    private String label;
    private Long flightCount;

    public DashboardRouteDTO() {}

    public DashboardRouteDTO(RoutePoint startPoint, RoutePoint endPoint){
        this(startPoint, endPoint, null);
    }

    public DashboardRouteDTO(RoutePoint startPoint, RoutePoint endPoint, Long flightCount){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.label = startPoint + " - " + endPoint;
        this.flightCount = flightCount;
    }

    public RoutePoint getStartPoint(){
        return startPoint;
    }

    public RoutePoint getEndPoint(){
        return endPoint;
    }

    public String getLable(){
        return label;
    }

    public Long getFlightCount(){
        return flightCount;
    }

    public void setStartPoint(RoutePoint startPoint){
        this.startPoint = startPoint;
    }

    public void setEndPoint(RoutePoint endPoint){
        this.endPoint = endPoint;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setFlightCount(Long flightCount){
        this.flightCount = flightCount;
    }
}
