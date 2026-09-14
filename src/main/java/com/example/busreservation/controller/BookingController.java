package com.example.busreservation.controller;

import com.example.busreservation.model.Booking;
import com.example.busreservation.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * BookingController - Handles booking management API endpoints
 * Used by: Passengers (create bookings), Customer Service (view bookings), Ticketing (manage tickets)
 * Functions: Create, read, update bookings, search by email/schedule
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Booking>> getBookingsByScheduleId(@PathVariable Integer scheduleId) {
        List<Booking> bookings = bookingService.getBookingsByScheduleId(scheduleId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Booking>> getBookingsByEmail(@PathVariable String email) {
        List<Booking> bookings = bookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(bookings);
    }


    @PostMapping("")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> req) {
        try {
            String firstName = (String) req.get("firstName");
            String lastName = (String) req.get("lastName");
            String phone = (String) req.get("phone");
            String email = (String) req.get("email");
            String nic = (String) req.get("nic");
            Integer bookedTickets = (Integer) req.get("bookedTickets");
            Integer scheduleId = (Integer) req.get("scheduleId");

            if (firstName == null || lastName == null || phone == null || 
                email == null || nic == null || bookedTickets == null || scheduleId == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            Optional<Booking> booking = bookingService.createBooking(
                firstName, lastName, phone, email, bookedTickets, scheduleId, nic
            );

            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.badRequest().body("Unable to create booking - check if schedule and user exist");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating booking: " + e.getMessage());
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createAdminBooking(@RequestBody Map<String, Object> req) {
        try {
            String firstName = (String) req.get("firstName");
            String lastName = (String) req.get("lastName");
            String phone = (String) req.get("phone");
            String email = (String) req.get("email");
            String nic = (String) req.get("nic");
            Integer bookedTickets = (Integer) req.get("bookedTickets");
            Integer scheduleId = (Integer) req.get("scheduleId");

            if (firstName == null || lastName == null || phone == null || 
                email == null || nic == null || bookedTickets == null || scheduleId == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            Optional<Booking> booking = bookingService.createAdminBooking(
                firstName, lastName, phone, email, bookedTickets, scheduleId, nic
            );

            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.badRequest().body("Unable to create admin booking - check if schedule and user exist");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating admin booking: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        try {
            Integer scheduleId = (Integer) req.get("scheduleId");
            Integer bookedTickets = (Integer) req.get("bookedTickets");

            if (scheduleId == null || bookedTickets == null) {
                return ResponseEntity.badRequest().body("Missing required fields: scheduleId and bookedTickets");
            }

            Optional<Booking> booking = bookingService.updateBooking(id, scheduleId, bookedTickets);

            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.badRequest().body("Unable to update booking - check if booking and schedule exist");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating booking: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/tickets")
    public ResponseEntity<?> updateBookingTickets(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        try {
            Integer bookedTickets = (Integer) req.get("bookedTickets");

            if (bookedTickets == null) {
                return ResponseEntity.badRequest().body("Missing required field: bookedTickets");
            }

            if (bookedTickets <= 0) {
                return ResponseEntity.badRequest().body("Number of tickets must be greater than 0");
            }

            Optional<Booking> booking = bookingService.updateBookingTicketsOnly(id, bookedTickets);

            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.badRequest().body("Unable to update booking - check if booking exists");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating booking tickets: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
        boolean success = bookingService.deleteBooking(id);
        if (success) {
            return ResponseEntity.ok().body("Booking deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Booking not found or could not be deleted");
        }
    }

}

