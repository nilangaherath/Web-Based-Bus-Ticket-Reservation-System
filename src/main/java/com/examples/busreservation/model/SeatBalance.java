package com.example.busreservation.model;

import jakarta.persistence.*;

/**
 * SeatBalance Model - Represents available seat count for each schedule
 * Used by: Operation Manager (manage seat availability), Ticketing (check availability), Passengers (view availability)
 * Contains: Seat availability information linked to bus and schedule
 */
@Entity
@Table(name = "SeatBalance")
public class SeatBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Integer balanceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    // Constructors
    public SeatBalance() {}

    public SeatBalance(Bus bus, Schedule schedule, Integer availableTickets) {
        this.bus = bus;
        this.schedule = schedule;
        this.availableTickets = availableTickets;
    }

    // Getters and Setters
    public Integer getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Integer balanceId) {
        this.balanceId = balanceId;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Integer getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }
}
