package com.example.busreservation.repository;

import com.example.busreservation.model.SeatBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatBalanceRepository extends JpaRepository<SeatBalance, Integer> {
    
    Optional<SeatBalance> findByScheduleScheduleId(Integer scheduleId);
    
    List<SeatBalance> findByBusBusId(Integer busId);
    
    @Query("SELECT s FROM SeatBalance s WHERE s.schedule.scheduleId = :scheduleId")
    Optional<SeatBalance> findByScheduleId(@Param("scheduleId") Integer scheduleId);
    
    @Query("SELECT s FROM SeatBalance s WHERE s.bus.busId = :busId")
    List<SeatBalance> findByBusId(@Param("busId") Integer busId);
    
    void deleteByScheduleScheduleId(Integer scheduleId);
}
