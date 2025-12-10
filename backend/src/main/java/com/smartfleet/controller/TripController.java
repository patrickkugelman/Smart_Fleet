package com.smartfleet.controller;

import com.smartfleet.dto.TripResponseDTO;
import com.smartfleet.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Trip Controller for trip management
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    /**
     * GET: Get all trips
     */
    @GetMapping
    public ResponseEntity<List<TripResponseDTO>> getAllTrips() {
        log.info("Fetching all trips");
        List<TripResponseDTO> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * GET: Get trip by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable Long id) {
        log.info("Fetching trip by ID: {}", id);
        TripResponseDTO trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    /**
     * GET: Get trips by driver ID
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripResponseDTO>> getTripsByDriver(@PathVariable Long driverId) {
        log.info("Fetching trips for driver: {}", driverId);
        List<TripResponseDTO> trips = tripService.getTripsByDriver(driverId);
        return ResponseEntity.ok(trips);
    }

    /**
     * GET: Get trips by vehicle ID
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TripResponseDTO>> getTripsByVehicle(@PathVariable Long vehicleId) {
        log.info("Fetching trips for vehicle: {}", vehicleId);
        List<TripResponseDTO> trips = tripService.getTripsByVehicle(vehicleId);
        return ResponseEntity.ok(trips);
    }

}
