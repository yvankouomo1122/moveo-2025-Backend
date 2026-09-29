package com.moveo.flight.api.service;

import com.moveo.flight.api.repository.DestinationRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.model.Destination;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.moveo.flight.api.dto.DestinationDTO;

@Service
public class DestinationService {
    private final DestinationRepository repository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public DestinationService(DestinationRepository repository,
    OperationLogRepository logRepository){
        this.repository = repository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /*
     * List all destinations entity
     */
    public List<DestinationDTO> getAllDestinations(){
        List<Destination> destinations = repository.findAll();
        return destinations.stream().map(this::toDTO).toList();
    }

    /*
     * Get Destination with given ID
     */
    public Optional<Destination> findById(Long id){
        return repository.findById(id);
    }

    /*
     * Get Destination startPoints
     */
    public List<String> getAllStartPoints(){
        List<String> startPoints = repository.getAllStartPoints();
        return startPoints;
    }

    /*
     * Get Destination endPoints corresponding to the startPoint
     */
    public List<String> getEndPoints(String startPoint){
        List<String> endPoints = repository.getEndPoints(startPoint);
        return endPoints;
    }
    /*
     * Create a destination
     */
    public Destination createDestination(Destination destination){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        Destination saved = repository.save(destination);
        operationLogService.logAction("CREATE_DESTINATION", "Destination", saved.getId(), authenticatedEmail, "Created destination");
        return saved;
    }

    /*
     * Update a destination
     */
    public Destination updateDestination(Long id, Destination updated){
        Destination existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Destination not found with id " + id));
        if (updated.getStartPoint() != null && !updated.getStartPoint().isBlank()){
            existing.setStartPoint(updated.getStartPoint());
        }
        if (updated.getEndPoint() != null && !updated.getEndPoint().isBlank()){
            existing.setEndPoint(updated.getEndPoint());
        }
        if (updated.getDistance() != null){
            existing.setDistance(updated.getDistance());
        }
        if (updated.getEstimatedDuration() != null){
            existing.setEstimatedDuration(updated.getEstimatedDuration());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        Destination saved = repository.save(existing);
        operationLogService.logAction("UPDATED_DESTINATION", "Destination", saved.getId(), authenticatedEmail, "Updated destination");
        return saved;
    }

    /*
     * Delete a destination with the help of the ID
     */
    public void deleteDestination(Long id){
        if (!repository.existsById(id)){
            throw new RuntimeException("Destination not found with id " + id);
        }
        repository.deleteById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("DELETED_DESTINATION", "Destination", repository.getReferenceById(id).getId(), authenticatedEmail, "Deleted destination");
    }

    public DestinationDTO toDTO(Destination destination){
        return new DestinationDTO(destination.getId(), destination.getStartPoint(), destination.getEndPoint(), destination.getDistance(), destination.getEstimatedDuration(), destination.getBasePrice());
    }
}
