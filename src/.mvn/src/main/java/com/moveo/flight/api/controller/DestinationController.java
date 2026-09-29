package com.moveo.flight.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moveo.flight.api.model.Destination;
import com.moveo.flight.api.service.DestinationService;
import com.moveo.flight.api.dto.DestinationDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {
    @Autowired
    private final DestinationService service;

    public DestinationController(DestinationService service){
        this.service = service;
    }

    // GET /destinations (ADMIN)
    @GetMapping
    public ResponseEntity<List<DestinationDTO>> getAllDestinations(){
        return ResponseEntity.ok(service.getAllDestinations());
    }

    // GET /destinations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Destination>> getDestinationById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    // GET /destinations/startPoints
    @GetMapping("/startPoints")
    public ResponseEntity<List<String>> getStartPoints(){
        return ResponseEntity.ok(service.getAllStartPoints());
    }

    // GET /destinations/endPoints?startPoint=Paris
    @GetMapping("/endPoints")
    public ResponseEntity<List<String>> getEndPoints(@RequestParam(required = false) String startPoint){
        return ResponseEntity.ok(service.getEndPoints(startPoint));
    }

    // POST /destinations (ADMIN)
    @PostMapping
    public ResponseEntity<Destination> createDestination(@RequestBody Destination destination){
        return ResponseEntity.ok(service.createDestination(destination));
    }

    // PUT /destinations/{id} (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Destination> updateDestination(@PathVariable Long id, @RequestBody Destination destination){
        return ResponseEntity.ok(service.updateDestination(id, destination));
    }

    // DELETE /destinations/{id} (ADMIN)
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDestination(@PathVariable Long id){
        service.deleteDestination(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message","Destination with id " + id + " deleted successfully");
        return response;
    }
}
