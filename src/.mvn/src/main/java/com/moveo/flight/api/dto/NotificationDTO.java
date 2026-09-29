package com.moveo.flight.api.dto;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private Long userId; // only the user ID, not the full user object
    private String type;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    // ✅ Constructor
    public NotificationDTO(Long id, Long userId, String type, String message, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ✅ Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

