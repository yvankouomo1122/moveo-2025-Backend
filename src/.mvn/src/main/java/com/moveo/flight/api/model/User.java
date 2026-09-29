package com.moveo.flight.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User entity representing application users.
 * - Supports basic user info
 * - Tracks audit timestamps
 * - Has relationships with Reservations & Notifications
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"), // Ensure email uniqueness
        @UniqueConstraint(columnNames = "phone_number") // Ensure phone number uniqueness
    }
)

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email; // Required & unique

    @Column(nullable = false, length = 255)
    @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) // Ignore password in JSON responses but accept during deserialization
    private String password; // Will later be hashed

    @Enumerated(EnumType.STRING)
    private UserRole roles; // Example of roles for now : ADMIN, USER

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "phone_number", length = 50, unique = true)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Created timestamp

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Updated timestamp

    /** One User → Many Reservations */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevents infinite recursion in JSON serialization with reservations
    private List<Reservation> reservations = new ArrayList<>();

    /*
     * User status before confirmation. PENDING_VERIFICATION, ACTIVATE, DISABLED.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /*
     * User OTP code for verification.
     * This will be used to verify the user after registration.
     */
    private String otpCode;

    /*
     * User OTP expiry time.
     */
    private LocalDateTime otpExpiry;

    /** Auto-fill timestamps before saving */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /** Auto-update timestamp when modifying */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors

    public User() {}

    public User(String email, String password, UserRole roles, String fullName, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters

    // Get id of user
    public Long  getId() {
        return id;
    }

    // Set id of user
    public void setId(Long id) {
        this.id = id;
    }

    // Get email of user
    public String getEmail() {
        return email;
    }

    // Set email of user
    public void setEmail(String email) {
        this.email = email;
    }

    // Get password of user
    public String getPassword() {
        return password;
    }

    // Set password of user
    public void setPassword(String password) {
        this.password = password;
    }

    // Get roles of user
    public UserRole getRoles() {
        return roles;
    }

    // Set roles of user
    public void setRoles(UserRole roles) {
        this.roles = roles;
    }

    // Get full name of user
    public String getFullName() {
        return fullName;
    }

    // Set full name of user
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Get phone number of user
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Set phone number of user
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Get reservations of user
    public List<Reservation> getReservations() {
        return reservations;
    }

    // Set reservations of user
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    // Get user status
    public UserStatus getStatus() {return status;}

    // Set status of user
    public void setStatus(UserStatus status) {this.status = status;}

    // Get user otp code
    public String getOtpCode() { return otpCode; }

    // Set user otp code
    public void setOtpCode(String otpCode) { this.otpCode = otpCode;}

    // Get user otp expiry
    public LocalDateTime getOtpExpiry() { return otpExpiry; }

    // Set user otp expiry
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    // Validate user is Administrator
    public boolean IsAdmin(){
        return this.getRoles() == UserRole.ADMIN;
    }

    // === Utility method ===

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public User orElseThrow(Object object) {
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}
