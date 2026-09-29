package com.moveo.flight.api.exception;

public class ReservationOverlapException extends RuntimeException{
    public ReservationOverlapException(String message){
        super(message);
    }
}
