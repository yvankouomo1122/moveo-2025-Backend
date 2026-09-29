package com.moveo.flight.api.service;

import com.moveo.flight.api.repository.RouteRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.model.Route;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.moveo.flight.api.dto.RouteDTO;

@Service
public class RouteService {
    private final RouteRepository repository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public RouteService(RouteRepository repository,
    OperationLogRepository logRepository){
        this.repository = repository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /*
     * List all routes entity
     */
    public List<RouteDTO> getAllRoutes(){
        List<Route> routes = repository.findAll();
        return routes.stream().map(this::toDTO).toList();
    }

    /*
     * Get Destination with given ID
     */
    public Optional<Route> findById(Long id){
        return repository.findById(id);
    }

    /*
     * Get Routes startPoints
     */
    public List<String> getAllStartPoints(){
        List<String> startPoints = repository.getAllStartPoints();
        return startPoints;
    }

    /*
     * Get Routes endPoints corresponding to the startPoint
     */
    public List<String> getEndPoints(String startPoint){
        List<String> endPoints = repository.getEndPoints(startPoint);
        return endPoints;
    }
    /*
     * Create a route
     */
    public Route createRoute(Route route){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        if(route.getStartPoint().equals(route.getEndPoint())){
            throw new RuntimeException("Le point de départ ne peut pas être identique au point d'arrivée");
        }
        Route saved = repository.save(route);
        operationLogService.logAction("CREATE_DESTINATION", "Destination", saved.getId(), authenticatedEmail, "Created destination");
        return saved;
    }

    /*
     * Update a route
     */
    public Route updateRoute(Long id, Route updated){
        Route existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Destination not found with id " + id));
        if (updated.getStartPoint().equals(updated.getEndPoint())){
            throw new RuntimeException("Le point de départ ne peut pas être identique au point d'arrivée");
        }
        if (updated.getStartPoint() != null){
            existing.setStartPoint(updated.getStartPoint());
        }
        if (updated.getEndPoint() != null){
            existing.setEndPoint(updated.getEndPoint());
        }
        if (updated.getDistance() != null){
            existing.setDistance(updated.getDistance());
        }
        if (updated.getEstimatedDuration() != null){
            existing.setEstimatedDuration(updated.getEstimatedDuration());
        }
         if (updated.getBasePrice() != null){
            existing.setBasePrice(updated.getBasePrice());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        Route saved = repository.save(existing);
        operationLogService.logAction("UPDATED_DESTINATION", "Route", saved.getId(), authenticatedEmail, "Updated route");
        return saved;
    }

    /*
     * Delete a destination with the help of the ID
     */
    public void deleteRoute(Long id){
        if (!repository.existsById(id)){
            throw new RuntimeException("Route not found with id " + id);
        }
        repository.deleteById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("DELETED_ROUTE", "Route", repository.getReferenceById(id).getId(), authenticatedEmail, "Deleted route");
    }

    public RouteDTO toDTO(Route route){
        return new RouteDTO(route.getId(), route.getStartPoint(), route.getEndPoint(), route.getDistance(), route.getEstimatedDuration(), route.getBasePrice());
    }
}
