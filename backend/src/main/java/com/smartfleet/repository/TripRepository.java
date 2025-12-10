package com.smartfleet.repository;

import com.smartfleet.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Trip entity
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Find all trips for a specific driver
     */
    List<Trip> findByDriverId(Long driverId);

    /**
     * Count trips for a specific driver
     */
    Long countByDriverId(Long driverId);

    /**
     * Find trips by status
     */
    List<Trip> findByStatus(String status);

    /**
     * Find trips for a specific vehicle
     */
    List<Trip> findByVehicleId(Long vehicleId);

}
