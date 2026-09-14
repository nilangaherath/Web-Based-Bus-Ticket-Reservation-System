package com.example.busreservation.controller;

import com.example.busreservation.model.Bus;
import com.example.busreservation.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * BusController - Handles bus fleet management API endpoints
 * Used by: Operation Manager (primary), Ticketing (view bus info)
 * Functions: Create, read, update, delete bus information
 */
@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*")
public class BusController {

    @Autowired
    private BusService busService;

    @GetMapping("")
    public ResponseEntity<List<Bus>> listBuses() {
        return ResponseEntity.ok(busService.listAllBuses());
    }

    @PostMapping("")
    public ResponseEntity<?> createBus(@RequestBody Bus bus) {
        try {
            Bus createdBus = busService.createBus(bus);
            return ResponseEntity.ok(createdBus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating bus: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBus(@PathVariable Integer id, @RequestBody Bus updates) {
        try {
            Optional<Bus> updated = busService.updateBus(id, updates);
            if (updated.isPresent()) return ResponseEntity.ok(updated.get());
            return ResponseEntity.badRequest().body("Bus not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable Integer id) {
        boolean deleted = busService.deleteBus(id);
        if (deleted) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("Bus not found");
    }
}
