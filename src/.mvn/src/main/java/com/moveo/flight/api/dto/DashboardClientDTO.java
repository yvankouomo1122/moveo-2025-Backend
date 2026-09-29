package com.moveo.flight.api.dto;

public class DashboardClientDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Long reservationCount;
    private String status;

    public DashboardClientDTO() {}

    public DashboardClientDTO(Long id, String fullName, String email, String phoneNumber, Long reservationCount, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.reservationCount = reservationCount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Long getReservationCount() {
        return reservationCount;
    }

    public String getStatus() {
        return status;
    }
}
