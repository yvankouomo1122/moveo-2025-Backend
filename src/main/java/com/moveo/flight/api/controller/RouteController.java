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

import com.moveo.flight.api.model.Route;
import com.moveo.flight.api.service.RouteService;
import com.moveo.flight.api.dto.RouteDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    @Autowired
    private final RouteService service;

    public RouteController(RouteService service){
        this.service = service;
    }

    // GET /routes (ADMIN)
    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAllRoutes(){
        return ResponseEntity.ok(service.getAllRoutes());
    }

    // GET /routes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Route>> getRouteById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    // GET /routes/startPoints
    @GetMapping("/startPoints")
    public ResponseEntity<List<String>> getStartPoints(){
        return ResponseEntity.ok(service.getAllStartPoints());
    }

    // GET /routes/endPoints?startPoint=Paris
    // GET /routes/endPoints
    @GetMapping("/endPoints")
    public ResponseEntity<List<String>> getEndPoints(@RequestParam(required = false) String startPoint){
        return ResponseEntity.ok(service.getEndPoints(startPoint));
    }

    // POST /routes (ADMIN)
    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody Route route){
        return ResponseEntity.ok(service.createRoute(route));
    }

    // PUT /routes/{id} (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody Route route){
        return ResponseEntity.ok(service.updateRoute(id, route));
    }

    // DELETE /routes/{id} (ADMIN)
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteRoute(@PathVariable Long id){
        service.deleteRoute(id);
        // Cree une reponse vue que le service ne renvoie rien
        Map<String, Object> response = new HashMap<>();
        response.put("message","Route with id " + id + " deleted successfully");
        return response;
    }
}
