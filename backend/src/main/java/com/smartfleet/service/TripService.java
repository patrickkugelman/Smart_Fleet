package com.smartfleet.service;

import com.smartfleet.dto.TripResponseDTO;
import com.smartfleet.entity.Driver;
import com.smartfleet.entity.Trip;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
    private final DriverRepository driverRepository; // Injectam repository-ul de soferi
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
     * Get Current Active Trip for Driver (NOU)
     */
    public TripResponseDTO getCurrentTripForDriver(Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);

        // Cautam o cursa care este ASSIGNED sau ON_TRIP
        return trips.stream()
                .filter(t -> "ASSIGNED".equals(t.getStatus()) || "ON_TRIP".equals(t.getStatus()))
                .sorted(Comparator.comparing(Trip::getCreatedAt).reversed()) // Cea mai recenta
                .findFirst()
                .map(this::mapToResponseDTO)
                .orElse(null);
    }

    /**
     * Start a trip (NOU)
     */
    @Transactional
    public TripResponseDTO startTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (!"ASSIGNED".equals(trip.getStatus())) {
            throw new RuntimeException("Trip cannot be started. Current status: " + trip.getStatus());
        }

        // Actualizam Trip-ul
        trip.setStatus("ON_TRIP");
        trip.setStartTime(LocalDateTime.now());

        // Actualizam Soferul -> ON_TRIP
        Driver driver = trip.getDriver();
        driver.setStatus("ON_TRIP");
        driverRepository.save(driver);

        trip = tripRepository.save(trip);
        log.info("Trip started: {}", tripId);
        return mapToResponseDTO(trip);
    }

    /**
     * Complete a trip (NOU)
     */
    @Transactional
    public TripResponseDTO completeTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        // Actualizam Trip-ul
        trip.setStatus("COMPLETED");
        trip.setEndTime(LocalDateTime.now());

        // Actualizam Soferul -> AVAILABLE (Liber pentru o noua cursa)
        Driver driver = trip.getDriver();
        driver.setStatus("AVAILABLE");
        driverRepository.save(driver);

        trip = tripRepository.save(trip);
        log.info("Trip completed: {}", tripId);
        return mapToResponseDTO(trip);
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

        if (tripDetails.getEndLocation() != null)
            trip.setEndLocation(tripDetails.getEndLocation());
        if (tripDetails.getKm() != null)
            trip.setKm(tripDetails.getKm());
        if (tripDetails.getDistance() != null)
            trip.setDistance(tripDetails.getDistance());
        if (tripDetails.getEndTime() != null)
            trip.setEndTime(tripDetails.getEndTime());
        if (tripDetails.getStatus() != null)
            trip.setStatus(tripDetails.getStatus());

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
        dto.setDistance(trip.getDistance() != null ? trip.getDistance() : trip.getKm());
        dto.setStatus(trip.getStatus());
        dto.setStartTime(trip.getStartTime() != null ? trip.getStartTime().format(formatter) : "");
        dto.setEndTime(trip.getEndTime() != null ? trip.getEndTime().format(formatter) : "");
        return dto;
    }
}