package com.example.busreservation.controller;

import com.example.busreservation.model.Route;
import com.example.busreservation.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * RouteController - Handles route management API endpoints
 * Used by: Operation Manager (primary), Passengers (view routes), Ticketing (view route info)
 * Functions: Create, read, update, delete route information
 */
@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping("")
    public ResponseEntity<List<Route>> listRoutes() {
        return ResponseEntity.ok(routeService.listAllRoutes());
    }

    @GetMapping("/district/{district}")
    public ResponseEntity<List<Route>> getRoutesByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(routeService.findByDistrict(district));
    }

    @PostMapping("")
    public ResponseEntity<?> createRoute(@RequestBody Route route) {
        try {
            Route created = routeService.createRoute(route);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating route: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Integer id, @RequestBody Route updates) {
        Optional<Route> updated = routeService.updateRoute(id, updates);
        if (updated.isPresent()) return ResponseEntity.ok(updated.get());
        return ResponseEntity.badRequest().body("Route not found");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Integer id) {
        boolean deleted = routeService.deleteRoute(id);
        if (deleted) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("Route not found");
    }
}


