package com.example.busreservation.repository;

import com.example.busreservation.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    List<Booking> findByScheduleScheduleId(Integer scheduleId);
    
    List<Booking> findByEmail(String email);

    long countByUserNic(String nic);
}

