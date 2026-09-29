package com.moveo.flight.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    // Search by model containing a keyword
    List<Flight> findByModelContainingIgnoreCase(String model);

    // Research by status
    List<Flight> findByStatus(FlightStatus status);

    // Combined search with optional filters
    @Query("""
        SELECT f FROM Flight f
        WHERE (:model IS NULL OR LOWER(f.model) LIKE LOWER(CONCAT('%', :model, '%')))
        AND (:status IS NULL OR f.status = :status)
        AND (:minPrice IS NULL OR f.pricePerHour >= :minPrice)
        AND (:maxPrice IS NULL OR f.pricePerHour <= :maxPrice)
    """)
    List<Flight> searchFlights(
        @Param("model") String model,
        @Param("status") FlightStatus status,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
    );
    
    // Count flights by status (available, reserved)
    long countByStatus(FlightStatus status);
}