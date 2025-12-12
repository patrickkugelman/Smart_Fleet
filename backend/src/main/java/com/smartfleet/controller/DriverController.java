package com.smartfleet.controller;

import com.smartfleet.dto.DriverResponseDTO;
import com.smartfleet.dto.TripDTO;
import com.smartfleet.dto.MessageResponse;
import com.smartfleet.entity.Driver;
import com.smartfleet.entity.Trip;
import com.smartfleet.entity.User;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.repository.TripRepository;
import com.smartfleet.repository.VehicleRepository;
import com.smartfleet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DriverController {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // --- GET CURRENT DRIVER ---
    @GetMapping("/me")
    public ResponseEntity<DriverResponseDTO> getCurrentDriver(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return driverRepository.findByUser(user)
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GET ALL DRIVERS ---
    @GetMapping
    // MODIFICAT: Permitem si DRIVER sa vada lista (pentru Desktop Client)
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        List<DriverResponseDTO> dtos = drivers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<DriverResponseDTO> getDriverById(@PathVariable Long id) {
        return driverRepository.findById(id)
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DELETE DRIVER ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        try {
            Driver driver = driverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            // 1. Dezlegam vehiculul
            if (driver.getVehicle() != null) {
                driver.setVehicle(null);
                driverRepository.save(driver);
            }

            // 2. Stergem user-ul asociat
            User user = driver.getUser();

            // 3. Stergem driver-ul
            driverRepository.delete(driver);

            if (user != null) {
                userRepository.delete(user);
            }

            return ResponseEntity.ok(new MessageResponse("Driver deleted successfully!"));
        } catch (Exception e) {
            log.error("Error deleting driver", e);
            return ResponseEntity.status(500).body(new MessageResponse("Delete failed: " + e.getMessage()));
        }
    }

    // --- UPLOAD AVATAR ---
    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (!"image/png".equals(file.getContentType())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Only PNG images are allowed!"));
            }
            Driver driver = driverRepository.findById(id).orElseThrow(() -> new RuntimeException("Driver not found"));
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            String filename = "driver_" + id + "_" + UUID.randomUUID() + ".png";
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + filename;
            driver.setAvatarUrl(fileUrl);
            driverRepository.save(driver);
            return ResponseEntity.ok(new MessageResponse("Avatar uploaded: " + fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Upload failed"));
        }
    }

    // --- UPDATE DRIVER ---
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return driverRepository.findById(id).map(driver -> {
            if (updates.containsKey("vehicleId")) {
                Object vId = updates.get("vehicleId");
                if (vId != null) {
                    Long vid = ((Number) vId).longValue();
                    Vehicle v = vehicleRepository.findById(vid).orElseThrow();
                    driver.setVehicle(v);
                } else {
                    driver.setVehicle(null);
                }
            }
            if (updates.containsKey("status"))
                driver.setStatus((String) updates.get("status"));
            return ResponseEntity.ok(convertToResponseDTO(driverRepository.save(driver)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- SELF ASSIGN VEHICLE ---
    @PostMapping("/assign-vehicle")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> assignVehicleToSelf(@RequestBody Map<String, Long> request, Principal principal) {
        Long vehicleId = request.get("vehicleId");
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Driver driver = driverRepository.findByUser(user).orElseThrow();

        if (driverRepository.existsByVehicleId(vehicleId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Vehicle already assigned"));
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        driver.setVehicle(vehicle);
        driver.setStatus("AVAILABLE");
        driverRepository.save(driver);
        return ResponseEntity.ok(new MessageResponse("Vehicle assigned!"));
    }

    // --- ASSIGN TRIP ---
    @PostMapping("/{driverId}/assign-trip")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripDTO> assignTrip(@PathVariable Long driverId, @RequestBody Map<String, Object> tripData) {
        try {
            Driver driver = driverRepository.findById(driverId).orElseThrow();
            if (driver.getVehicle() == null)
                return ResponseEntity.badRequest().build();

            Trip trip = new Trip();
            trip.setDriver(driver);
            trip.setVehicle(driver.getVehicle());
            trip.setStartLocation((String) tripData.get("startLocation"));
            trip.setEndLocation((String) tripData.get("endLocation"));
            trip.setStatus("ASSIGNED");

            if (tripData.containsKey("distance")) {
                Object d = tripData.get("distance");
                trip.setDistance(d instanceof Number ? ((Number) d).doubleValue() : Double.parseDouble(d.toString()));
                trip.setKm(trip.getDistance());
            }

            if (tripData.containsKey("startTime")) {
                try {
                    trip.setStartTime(LocalDateTime.parse((String) tripData.get("startTime")));
                } catch (Exception e) {
                    trip.setStartTime(LocalDateTime.now());
                }
            } else {
                trip.setStartTime(LocalDateTime.now());
            }

            driver.setStatus("ON_TRIP");
            driverRepository.save(driver);
            return ResponseEntity.ok(convertTripToDTO(tripRepository.save(trip)));
        } catch (Exception e) {
            log.error("Trip error", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{driverId}/trips")
    public ResponseEntity<List<TripDTO>> getDriverTrips(@PathVariable Long driverId) {
        return ResponseEntity.ok(tripRepository.findByDriverId(driverId).stream().map(this::convertTripToDTO)
                .collect(Collectors.toList()));
    }

    // === CONVERTOARE ===
    private DriverResponseDTO convertToResponseDTO(Driver driver) {
        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setLicense(driver.getLicense());
        dto.setStatus(driver.getStatus());
        dto.setAvatarUrl(driver.getAvatarUrl());

        if (driver.getUser() != null) {
            dto.setUsername(driver.getUser().getUsername());
            dto.setEmail(driver.getUser().getEmail());
        }
        if (driver.getVehicle() != null) {
            dto.setVehicleId(driver.getVehicle().getId());
            dto.setVehiclePlate(driver.getVehicle().getPlate());
            dto.setVehicleBrand(driver.getVehicle().getBrand());
        }
        Long count = tripRepository.countByDriverId(driver.getId());
        dto.setTripCount(count != null ? count.intValue() : 0);

        dto.setCreatedAt(driver.getCreatedAt() != null ? driver.getCreatedAt().format(formatter) : "");
        return dto;
    }

    private TripDTO convertTripToDTO(Trip trip) {
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