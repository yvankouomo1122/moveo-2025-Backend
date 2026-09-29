package com.moveo.flight.api.dto;

public class VerifyOtpDTO {
    private String email;
    private String otpCode;

    // Empty constructor for Jackson
    public VerifyOtpDTO() {}

    // Constructor for easy mapping
    public VerifyOtpDTO(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    // Getters (needed for JSON serialization)
    public String getEmail() {return email;}
    public String getOtpCode() {return otpCode;}

    // Setters 
    public void setEmail(String email) {this.email = email;}
    public void setOtpCode(String otpCode) {this.otpCode = otpCode;}

}
