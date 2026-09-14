package com.example.busreservation.service;

import com.example.busreservation.model.Booking;
import com.example.busreservation.model.Schedule;
import com.example.busreservation.model.User;
import com.example.busreservation.repository.BookingRepository;
import com.example.busreservation.repository.ScheduleRepository;
import com.example.busreservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SeatBalanceService seatBalanceService;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Integer bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getBookingsByScheduleId(Integer scheduleId) {
        return bookingRepository.findByScheduleScheduleId(scheduleId);
    }

    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByEmail(email);
    }

    @Transactional
    public Optional<Booking> createBooking(String firstName, String lastName, String phone, 
                                         String email, Integer bookedTickets, Integer scheduleId, String nic) {
        try {
            validateTicketCount(bookedTickets);
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return Optional.empty();
            }
            
            // Find user by NIC
            Optional<User> userOpt = userRepository.findByNic(nic);
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Schedule schedule = scheduleOpt.get();
            ensureSeatsAvailable(scheduleId, bookedTickets);
            User user = userOpt.get();
            Booking booking = new Booking(firstName, lastName, phone, email, bookedTickets, schedule, user);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update seat balance
            seatBalanceService.updateSeatBalanceForBooking(scheduleId, bookedTickets);
            
            return Optional.of(savedBooking);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Booking> createAdminBooking(String firstName, String lastName, String phone, 
                                               String email, Integer bookedTickets, Integer scheduleId, String nic) {
        try {
            validateTicketCount(bookedTickets);
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return Optional.empty();
            }
            
            // Find user by NIC
            Optional<User> userOpt = userRepository.findByNic(nic);
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Schedule schedule = scheduleOpt.get();
            ensureSeatsAvailable(scheduleId, bookedTickets);
            if (schedule.getFare() == null) {
                throw new IllegalArgumentException("Schedule fare is required before creating an admin booking");
            }
            User user = userOpt.get();
            Booking booking = new Booking(firstName, lastName, phone, email, bookedTickets, schedule, user);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update seat balance
            seatBalanceService.updateSeatBalanceForBooking(scheduleId, bookedTickets);
            
            // Create payment record with bank method for admin bookings
            BigDecimal totalPrice = schedule.getFare().multiply(BigDecimal.valueOf(bookedTickets));
            paymentService.createPayment(
                savedBooking.getBookingId(), 
                "bank", 
                "paid",
                totalPrice
            );
            
            return Optional.of(savedBooking);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating admin booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Booking> updateBooking(Integer bookingId, Integer newScheduleId, Integer newBookedTickets) {
        try {
            validateTicketCount(newBookedTickets);
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(newScheduleId);
            if (scheduleOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Booking booking = bookingOpt.get();
            Schedule newSchedule = scheduleOpt.get();
            
            // Get old values for seat balance adjustment
            Integer oldScheduleId = booking.getSchedule().getScheduleId();
            Integer oldBookedTickets = booking.getBookedTickets();
            ensureSeatsAvailableForUpdate(oldScheduleId, newScheduleId, oldBookedTickets, newBookedTickets);
            
            // Update booking
            booking.setSchedule(newSchedule);
            booking.setBookedTickets(newBookedTickets);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update seat balance - add back old tickets, subtract new tickets
            if (!oldScheduleId.equals(newScheduleId)) {
                // Different schedule - adjust both schedules
                seatBalanceService.updateSeatBalanceForCancellation(oldScheduleId, oldBookedTickets);
                seatBalanceService.updateSeatBalanceForBooking(newScheduleId, newBookedTickets);
            } else {
                // Same schedule - adjust ticket count difference
                int ticketDifference = newBookedTickets - oldBookedTickets;
                if (ticketDifference > 0) {
                    seatBalanceService.updateSeatBalanceForBooking(newScheduleId, ticketDifference);
                } else if (ticketDifference < 0) {
                    seatBalanceService.updateSeatBalanceForCancellation(newScheduleId, Math.abs(ticketDifference));
                }
            }
            
            // Update payment record with new total price
            if (newSchedule.getFare() != null) {
                BigDecimal newTotalPrice = newSchedule.getFare().multiply(BigDecimal.valueOf(newBookedTickets));
                paymentService.updatePaymentForBooking(bookingId, newTotalPrice);
            }
            
            return Optional.of(savedBooking);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Booking> updateBookingTicketsOnly(Integer bookingId, Integer newBookedTickets) {
        try {
            validateTicketCount(newBookedTickets);
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Booking booking = bookingOpt.get();
            Schedule currentSchedule = booking.getSchedule();
            
            // Get old values for seat balance adjustment
            Integer oldBookedTickets = booking.getBookedTickets();
            ensureSeatsAvailableForUpdate(
                currentSchedule.getScheduleId(),
                currentSchedule.getScheduleId(),
                oldBookedTickets,
                newBookedTickets
            );
            
            // Update only ticket count
            booking.setBookedTickets(newBookedTickets);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            // Update seat balance - adjust ticket count difference
            int ticketDifference = newBookedTickets - oldBookedTickets;
            if (ticketDifference > 0) {
                seatBalanceService.updateSeatBalanceForBooking(currentSchedule.getScheduleId(), ticketDifference);
            } else if (ticketDifference < 0) {
                seatBalanceService.updateSeatBalanceForCancellation(currentSchedule.getScheduleId(), Math.abs(ticketDifference));
            }
            
            // Update payment record with new total price
            if (currentSchedule.getFare() != null) {
                BigDecimal newTotalPrice = currentSchedule.getFare().multiply(BigDecimal.valueOf(newBookedTickets));
                paymentService.updatePaymentForBooking(bookingId, newTotalPrice);
            }
            
            return Optional.of(savedBooking);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating booking tickets: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteBooking(Integer bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                Integer scheduleId = booking.getSchedule().getScheduleId();
                Integer bookedTickets = booking.getBookedTickets();
                
                // Delete associated notifications first (to avoid FK constraint violations)
                notificationService.deleteNotificationsByBookingId(bookingId);
                
                // Delete associated payment(s)
                paymentService.deletePaymentsByBookingId(bookingId);
                
                // Delete the booking
                bookingRepository.deleteById(bookingId);
                
                // Update seat balance
                seatBalanceService.updateSeatBalanceForCancellation(scheduleId, bookedTickets);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            return false;
        }
    }

    private void validateTicketCount(Integer bookedTickets) {
        if (bookedTickets == null || bookedTickets <= 0) {
            throw new IllegalArgumentException("Booked tickets must be greater than 0");
        }
    }

    private void ensureSeatsAvailable(Integer scheduleId, Integer requestedTickets) {
        Integer availableTickets = seatBalanceService.getAvailableTickets(scheduleId);
        if (availableTickets < requestedTickets) {
            throw new IllegalArgumentException("Only " + availableTickets + " seat(s) available for this schedule");
        }
    }

    private void ensureSeatsAvailableForUpdate(Integer oldScheduleId, Integer newScheduleId,
                                               Integer oldBookedTickets, Integer newBookedTickets) {
        Integer availableTickets = seatBalanceService.getAvailableTickets(newScheduleId);
        if (oldScheduleId.equals(newScheduleId)) {
            availableTickets += oldBookedTickets;
        }

        if (availableTickets < newBookedTickets) {
            throw new IllegalArgumentException("Only " + availableTickets + " seat(s) available for this schedule");
        }
    }
}
