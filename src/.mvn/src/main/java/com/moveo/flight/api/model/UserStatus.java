package com.moveo.flight.api.model;

public enum UserStatus {
    PENDING_VERIFICATION, // User has registered but not yet verified
    ACTIVE,               // User is active and can log in
    DISABLED              // User is disabled and cannot log in
}
