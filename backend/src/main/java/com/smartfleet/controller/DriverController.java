package com.smartfleet.controller;

import com.smartfleet.dto.DriverDTO;
import com.smartfleet.dto.TripDTO;
import com.smartfleet.entity.Driver;
import com.smartfleet.entity.Trip;
import com.smartfleet.entity.User;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.repository.TripRepository;
import com.smartfleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Driver Management
 */
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    /**
     * Get all drivers (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverDTO>> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        List<DriverDTO> driverDTOs = drivers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(driverDTOs);
    }

    /**
     * Get driver by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable Long id) {
        return driverRepository.findById(id)
                .map(driver -> ResponseEntity.ok(convertToDTO(driver)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update driver (assign vehicle)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverDTO> updateDriver(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        return driverRepository.findById(id)
                .map(driver -> {
                    // Update vehicle assignment
                    if (updates.containsKey("vehicleId")) {
                        Object vehicleIdObj = updates.get("vehicleId");
                        if (vehicleIdObj != null) {
                            Long vehicleId = ((Number) vehicleIdObj).longValue();
                            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
                            driver.setVehicle(vehicle);
                        } else {
                            driver.setVehicle(null);
                        }
                    }

                    // Update status if provided
                    if (updates.containsKey("status")) {
                        driver.setStatus((String) updates.get("status"));
                    }

                    Driver updated = driverRepository.save(driver);
                    return ResponseEntity.ok(convertToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Assign trip to driver
     */
    @PostMapping("/{driverId}/assign-trip")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripDTO> assignTrip(
            @PathVariable Long driverId,
            @RequestBody Map<String, Object> tripData) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getVehicle() == null) {
            return ResponseEntity.badRequest().build();
        }

        Trip trip = new Trip();
        trip.setDriver(driver);
        trip.setVehicle(driver.getVehicle());
        trip.setStartLocation((String) tripData.get("startLocation"));
        trip.setEndLocation((String) tripData.get("endLocation"));
        trip.setStatus("ASSIGNED");

        if (tripData.containsKey("distance")) {
            Double distance = ((Number) tripData.get("distance")).doubleValue();
            trip.setDistance(distance);
            trip.setKm(distance); // Sync with legacy field
        }

        if (tripData.containsKey("startTime")) {
            trip.setStartTime(LocalDateTime.parse((String) tripData.get("startTime")));
        } else {
            trip.setStartTime(LocalDateTime.now());
        }

        Trip saved = tripRepository.save(trip);
        return ResponseEntity.ok(convertTripToDTO(saved));
    }

    /**
     * Get trips for a driver
     */
    @GetMapping("/{driverId}/trips")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<List<TripDTO>> getDriverTrips(@PathVariable Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        List<TripDTO> tripDTOs = trips.stream()
                .map(this::convertTripToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tripDTOs);
    }

    /**
     * Convert Driver to DTO
     */
    private DriverDTO convertToDTO(Driver driver) {
        DriverDTO dto = new DriverDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setLicense(driver.getLicense());
        dto.setStatus(driver.getStatus());
        dto.setUserId(driver.getUser().getId());

        User user = driver.getUser();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        if (driver.getVehicle() != null) {
            dto.setVehicleId(driver.getVehicle().getId());
            dto.setVehiclePlate(driver.getVehicle().getPlate());
            dto.setVehicleBrand(driver.getVehicle().getBrand());
        }

        // Count trips
        Long tripCount = tripRepository.countByDriverId(driver.getId());
        dto.setTripCount(tripCount != null ? tripCount.intValue() : 0);

        return dto;
    }

    /**
     * Convert Trip to DTO
     */
    private TripDTO convertTripToDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setDriverId(trip.getDriver().getId());
        dto.setDriverName(trip.getDriver().getName());
        dto.setVehicleId(trip.getVehicle().getId());
        dto.setVehiclePlate(trip.getVehicle().getPlate());
        dto.setStartLocation(trip.getStartLocation());
        dto.setEndLocation(trip.getEndLocation());
        dto.setStartTime(trip.getStartTime());
        dto.setEndTime(trip.getEndTime());
        dto.setStatus(trip.getStatus());
        dto.setDistance(trip.getDistance() != null ? trip.getDistance() : trip.getKm());
        return dto;
    }
}