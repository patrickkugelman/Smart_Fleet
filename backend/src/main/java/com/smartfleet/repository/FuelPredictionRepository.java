package com.smartfleet.repository;

import com.smartfleet.entity.FuelPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FuelPrediction entity operations
 */
@Repository
public interface FuelPredictionRepository extends JpaRepository<FuelPrediction, Long> {
    List<FuelPrediction> findByTripId(Long tripId);
}
