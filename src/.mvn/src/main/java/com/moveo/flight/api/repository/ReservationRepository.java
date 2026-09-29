package com.moveo.flight.api.repository;

import com.moveo.flight.api.model.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Reservation entity.
 * Provides CRUD operations and custom queries.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Get all reservations for a specific user
    List<Reservation> findByUserId(Long userId);

    // Get all reservations for a specific flight
    List<Reservation> findByFlightId(Long flightId);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count distinct users who made reservations between start and end dates
    @Query("SELECT COUNT(DISTINCT r.user.id) FROM Reservation r WHERE r.createdAt BETWEEN :start AND :end")
    Long countDistinctUsersByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Calculate the sum of total prices for reservations created between start and end dates
    @Query("""
            SELECT SUM(r.totalPrice)
            FROM Reservation r 
            WHERE (COALESCE(:start, r.createdAt) <= r.createdAt)
            AND (COALESCE(:end, r.createdAt) >= r.createdAt)
        """)    
    java.math.BigDecimal sumEstimatedRevenueBetween(LocalDateTime start, LocalDateTime end);

    // Count any overlapping reservation based on the flight, reservation startDate and endDate
    // Only blocks if there's an existing reservation with PENDING or CONFIRMED status
    // Ignores CANCELED or CLOSED reservations
    @Query("""
            SELECT COUNT(r) > 0 
            FROM Reservation r
            WHERE r.flight.id = :flightId
            AND r.status IN ('PENDING', 'CONFIRMED')
            AND (r.startDate < :endDate AND r.endDate > :startDate)
            """)
    boolean existsOverlappingReservation(@Param("flightId") Long flightId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count reservations by reservation date
    long countByStartDate(LocalDateTime startDate);

    @Query("""
            SELECT d.id, d.startPoint, d.endPoint, COUNT(r.id)
            FROM Reservation r
            JOIN r.payment p
            JOIN r.destination d
            WHERE p.paymentStatus = com.moveo.flight.api.model.PaymentStatus.PAID
            GROUP BY d.id, d.startPoint, d.endPoint
            ORDER BY COUNT(r.id) DESC
            """)
    List<Object[]> findPaidDestinationStats();

    @Query("""
            SELECT f.id, f.model, f.capacity, f.status, f.pricePerHour, f.images, COUNT(r.id)
            FROM Reservation r
            JOIN r.payment p
            JOIN r.flight f
            WHERE p.paymentStatus = com.moveo.flight.api.model.PaymentStatus.PAID
            GROUP BY f.id, f.model, f.capacity, f.status, f.pricePerHour, f.images
            ORDER BY COUNT(r.id) DESC, f.id ASC
            """)
    List<Object[]> findMostChosenPaidFlight();

    List<Reservation> findTop5ByOrderByStartDateDesc();
}
