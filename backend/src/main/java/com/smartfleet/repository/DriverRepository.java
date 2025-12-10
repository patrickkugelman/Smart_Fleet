package com.smartfleet.repository;

import com.smartfleet.entity.Driver;
import com.smartfleet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Driver entity operations
 * CRITICAL: Supports vehicle assignment queries
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);

    Optional<Driver> findByUser(User user);

    List<Driver> findByStatus(String status);

    boolean existsByLicense(String license);

    // CRITICAL: Check if vehicle is already assigned to a driver
    boolean existsByVehicleId(Long vehicleId);

    // Find driver by vehicle
    Optional<Driver> findByVehicleId(Long vehicleId);
}
