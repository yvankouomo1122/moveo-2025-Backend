package com.moveo.flight.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.moveo.flight.api.model.Route;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>{
    // Get all startPoints
    @Query("""
            SELECT DISTINCT r.startPoint from Route r
            """)
    List<String> getAllStartPoints();

    // Get corresponding endPoints to a startPoint
    @Query("""
            SELECT DISTINCT r.endPoint 
            from Route r
            where r.startPoint = :startPoint
            """)
    List<String> getEndPoints(@Param("startPoint") String startPoint);
}
