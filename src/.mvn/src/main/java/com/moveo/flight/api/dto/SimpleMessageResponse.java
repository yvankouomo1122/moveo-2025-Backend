package com.moveo.flight.api.dto;

public class SimpleMessageResponse {
    private boolean success;
    private String message;

    public SimpleMessageResponse() {}

    public SimpleMessageResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
