package com.example.busreservation.repository;

import com.example.busreservation.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
    
    List<Route> findByDistrict(String district);
}


