package com.smartfleet.controller;

import com.smartfleet.dto.TripDTO;
import com.smartfleet.dto.TripResponseDTO; // Asigură-te că ai importat DTO-ul corect
import com.smartfleet.entity.Trip;
import com.smartfleet.repository.TripRepository;
import com.smartfleet.service.TripService; // Import Service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TripController {

    private final TripRepository tripRepository;
    private final TripService tripService; // Injectam Service-ul

    // --- GET TRIPS FOR DRIVER ---
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripDTO>> getTripsByDriver(@PathVariable Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        return ResponseEntity.ok(trips.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    // --- START TRIP (NOU) ---
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<TripResponseDTO> startTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.startTrip(id));
    }

    // --- COMPLETE TRIP (NOU) ---
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<TripResponseDTO> completeTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.completeTrip(id));
    }

    // --- DELETE TRIP ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        if (tripRepository.existsById(id)) {
            tripRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper
    private TripDTO convertToDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setDriverId(trip.getDriver() != null ? trip.getDriver().getId() : null);
        dto.setDriverName(trip.getDriver() != null ? trip.getDriver().getName() : null);
        dto.setVehicleId(trip.getVehicle() != null ? trip.getVehicle().getId() : null);
        dto.setVehiclePlate(trip.getVehicle() != null ? trip.getVehicle().getPlate() : null);
        dto.setStartLocation(trip.getStartLocation());
        dto.setEndLocation(trip.getEndLocation());
        dto.setStatus(trip.getStatus());
        dto.setDistance(trip.getDistance());
        dto.setStartTime(trip.getStartTime());
        dto.setEndTime(trip.getEndTime());
        return dto;
    }
}