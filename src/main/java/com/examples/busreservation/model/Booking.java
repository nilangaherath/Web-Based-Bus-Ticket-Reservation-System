package com.example.busreservation.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Booking Model - Represents passenger booking information
 * Used by: Passengers (create bookings), Customer Service (view bookings), Ticketing (manage tickets)
 * Contains: Booking details, passenger info, schedule reference, and ticket count
 */
@Entity
@Table(name = "Booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId; // Unique booking identifier

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName; // Passenger's first name

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName; // Passenger's last name

    @Column(name = "phone", nullable = false, length = 15)
    private String phone; // Passenger's contact number

    @Column(name = "email", nullable = false, length = 100)
    private String email; // Passenger's email address

    @Column(name = "booked_tickets", nullable = false)
    private Integer bookedTickets; // Number of tickets booked

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // Reference to the bus schedule

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nic", referencedColumnName = "nic", nullable = false)
    private User user; // Reference to the user who made the booking

    // Constructors
    public Booking() {}

    public Booking(String firstName, String lastName, String phone, String email, 
                   Integer bookedTickets, Schedule schedule, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.bookedTickets = bookedTickets;
        this.schedule = schedule;
        this.user = user;
    }

    // Getters and Setters
    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getBookedTickets() {
        return bookedTickets;
    }

    public void setBookedTickets(Integer bookedTickets) {
        this.bookedTickets = bookedTickets;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

