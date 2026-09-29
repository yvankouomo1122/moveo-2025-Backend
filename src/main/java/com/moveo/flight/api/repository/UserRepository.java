package com.moveo.flight.api.repository;

import com.moveo.flight.api.dto.DashboardClientDTO;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for User entity.
 * 
 * Provides:
 *  - CRUD operations
 *  - Custom query to find by email
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find a user by email (for login)
    Optional<User> findByEmail(String email);

    // Check if an email already exists
    boolean existsByEmail(String email);

    // Count users by role
    long countByRoles(UserRole roles);

    @Query("""
        SELECT new com.moveo.flight.api.dto.DashboardClientDTO(
            u.id,
            u.fullName,
            u.email,
            u.phoneNumber,
            COUNT(r.id),
            CAST(u.status AS string)
        )
        FROM User u
        LEFT JOIN u.reservations r
        WHERE u.roles = com.moveo.flight.api.model.UserRole.USER
        GROUP BY u.id, u.fullName, u.email, u.phoneNumber, u.status
        ORDER BY u.id DESC
    """)
    List<DashboardClientDTO> findDashboardClients();
}
