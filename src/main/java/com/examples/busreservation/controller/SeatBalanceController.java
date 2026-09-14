package com.example.busreservation.controller;

import com.example.busreservation.model.SeatBalance;
import com.example.busreservation.service.SeatBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SeatBalanceController - Handles seat availability management API endpoints
 * Used by: Operation Manager (manage seat availability), Ticketing (check availability), Passengers (view availability)
 * Functions: Check available tickets, refresh seat balances, manage seat inventory
 */
@RestController
@RequestMapping("/api/seat-balance")
@CrossOrigin(origins = "*")
public class SeatBalanceController {

    @Autowired
    private SeatBalanceService seatBalanceService;

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<Map<String, Integer>> getAvailableTickets(@PathVariable Integer scheduleId) {
        Integer availableTickets = seatBalanceService.getAvailableTickets(scheduleId);
        return ResponseEntity.ok(Map.of("availableTickets", availableTickets));
    }

    @GetMapping("/schedule/{scheduleId}/details")
    public ResponseEntity<?> getSeatBalanceDetails(@PathVariable Integer scheduleId) {
        Optional<SeatBalance> seatBalance = seatBalanceService.getSeatBalanceByScheduleId(scheduleId);
        if (seatBalance.isPresent()) {
            return ResponseEntity.ok(seatBalance.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/schedule/{scheduleId}/refresh")
    public ResponseEntity<?> refreshSeatBalance(@PathVariable Integer scheduleId) {
        try {
            SeatBalance updated = seatBalanceService.createOrUpdateSeatBalance(scheduleId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error refreshing seat balance: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-all")
    public ResponseEntity<?> refreshAllSeatBalances() {
        try {
            seatBalanceService.refreshAllSeatBalances();
            return ResponseEntity.ok().body("All seat balances refreshed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error refreshing all seat balances: " + e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<List<SeatBalance>> getAllSeatBalances() {
        List<SeatBalance> allBalances = seatBalanceService.getAllSeatBalances();
        return ResponseEntity.ok(allBalances);
    }

    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseEntity<?> deleteSeatBalance(@PathVariable Integer scheduleId) {
        try {
            seatBalanceService.deleteSeatBalance(scheduleId);
            return ResponseEntity.ok().body("Seat balance record deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting seat balance: " + e.getMessage());
        }
    }
}
