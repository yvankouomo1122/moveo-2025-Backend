package com.moveo.flight.api.dto;

import com.moveo.flight.api.model.UserStatus;
import com.moveo.flight.api.model.UserRole;

public class UserDTO {

    private Long id;
    private String email;
    private UserRole roles;
    private String fullName;
    private String phoneNumber;
    private UserStatus status;
    private String message;

    // Constructor
    public UserDTO(Long id, String email, UserRole roles, String fullName,
                           String phoneNumber, UserStatus status, String message) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.message = message;
    }

    // Getters & setters (or use Lombok @Data if preferred)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public UserRole getRoles() {
        return roles;
    }
    public void setRoles(UserRole roles) {
        this.roles = roles;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
