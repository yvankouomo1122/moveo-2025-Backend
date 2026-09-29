package com.moveo.flight.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String action; // e.g : CREATE_RESERVATION, DELETE_DESTINATION
    private String entity; // e.g : "Reservation", "Destination"
    private Long entityId; // ID of the entity
    private String username;
    private LocalDateTime timestamp; 
    private String details;

    // Setters
    public void setAction(String action){this.action = action;}
    public void setEntity(String entity){this.entity = entity;}
    public void setEntityId(Long entityId){this.entityId = entityId;}
    public void setUsername(String username){this.username = username;}
    public void setTimestamp(LocalDateTime timestamp){this.timestamp = timestamp;}
    public void setDetails(String details){this.details = details;}
}
