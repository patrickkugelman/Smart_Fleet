package com.smartfleet.service;

import com.smartfleet.dto.TripResponseDTO;
import com.smartfleet.entity.Trip;
import com.smartfleet.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trip Service for managing trips
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Get all trips
     */
    public List<TripResponseDTO> getAllTrips() {
        return tripRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trip by ID
     */
    public TripResponseDTO getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
        return mapToResponseDTO(trip);
    }

    /**
     * Get trips by driver ID
     */
    public List<TripResponseDTO> getTripsByDriver(Long driverId) {
        return tripRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trips by vehicle ID
     */
    public List<TripResponseDTO> getTripsByVehicle(Long vehicleId) {
        return tripRepository.findByVehicleId(vehicleId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new trip
     */
    @Transactional
    public TripResponseDTO createTrip(Trip trip) {
        trip = tripRepository.save(trip);
        log.info("Trip created: Driver {} -> Vehicle {}", trip.getDriver().getId(), trip.getVehicle().getId());
        return mapToResponseDTO(trip);
    }

    /**
     * Update trip
     */
    @Transactional
    public TripResponseDTO updateTrip(Long id, Trip tripDetails) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));

        if (tripDetails.getEndLocation() != null) trip.setEndLocation(tripDetails.getEndLocation());
        if (tripDetails.getEndLat() != null) trip.setEndLat(tripDetails.getEndLat());
        if (tripDetails.getEndLng() != null) trip.setEndLng(tripDetails.getEndLng());
        if (tripDetails.getKm() != null) trip.setKm(tripDetails.getKm());
        if (tripDetails.getEndTime() != null) trip.setEndTime(tripDetails.getEndTime());

        trip = tripRepository.save(trip);
        return mapToResponseDTO(trip);
    }

    /**
     * Map Trip entity to ResponseDTO
     */
    private TripResponseDTO mapToResponseDTO(Trip trip) {
        TripResponseDTO dto = new TripResponseDTO();
        dto.setId(trip.getId());
        if (trip.getDriver() != null) {
            dto.setDriverName(trip.getDriver().getName());
        }
        if (trip.getVehicle() != null) {
            dto.setVehiclePlate(trip.getVehicle().getPlate());
        }
        dto.setStartLocation(trip.getStartLocation());
        dto.setEndLocation(trip.getEndLocation());
        dto.setKm(trip.getKm());
        dto.setStartTime(trip.getStartTime() != null ? trip.getStartTime().format(formatter) : "");
        dto.setEndTime(trip.getEndTime() != null ? trip.getEndTime().format(formatter) : "");
        return dto;
    }

}
