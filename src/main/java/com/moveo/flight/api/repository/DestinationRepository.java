package com.moveo.flight.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.moveo.flight.api.model.Destination;
import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>{
    // Get all startPoints
    @Query("""
            SELECT DISTINCT startPoint from Destination
            """)
    List<String> getAllStartPoints();

    // Get corresponding endPoints to a startPoint
    @Query("""
            SELECT DISTINCT endPoint from Destination where startPoint = :startPoint
            """)
    List<String> getEndPoints(@Param("startPoint") String startPoint);
}
