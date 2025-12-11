package com.smartfleet.controller;

import com.smartfleet.dto.TripDTO;
import com.smartfleet.entity.Trip;
import com.smartfleet.repository.TripRepository;
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

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripDTO>> getTripsByDriver(@PathVariable Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        return ResponseEntity.ok(trips.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    // === ENDPOINT NOU: DELETE TRIP ===
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
        dto.setStartLocation(trip.getStartLocation());
        dto.setEndLocation(trip.getEndLocation());
        dto.setStatus(trip.getStatus());
        dto.setDistance(trip.getDistance());
        dto.setStartTime(trip.getStartTime());
        dto.setEndTime(trip.getEndTime());
        return dto;
    }
}