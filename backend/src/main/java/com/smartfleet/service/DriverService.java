package com.smartfleet.service;

import com.smartfleet.dto.DriverResponseDTO;
import com.smartfleet.entity.Driver;
import com.smartfleet.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Driver Service for managing drivers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Get all drivers
     */
    public List<DriverResponseDTO> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get driver by ID
     */
    public DriverResponseDTO getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        return mapToResponseDTO(driver);
    }

    /**
     * Get drivers by status
     */
    public List<DriverResponseDTO> getDriversByStatus(String status) {
        return driverRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get driver by user ID
     */
    public DriverResponseDTO getDriverByUserId(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Driver not found for user: " + userId));
        return mapToResponseDTO(driver);
    }

    /**
     * Update driver status
     */
    @Transactional
    public DriverResponseDTO updateDriverStatus(Long id, String status) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        driver.setStatus(status);
        driver = driverRepository.save(driver);
        log.info("Driver status updated: {} -> {}", driver.getName(), status);
        return mapToResponseDTO(driver);
    }

    /**
     * Map Driver entity to ResponseDTO
     * CRITICAL: Includes vehicleId for vehicle assignment tracking
     */
    private DriverResponseDTO mapToResponseDTO(Driver driver) {
        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setLicense(driver.getLicense());
        dto.setStatus(driver.getStatus());
        if (driver.getUser() != null) {
            dto.setUsername(driver.getUser().getUsername());
            dto.setEmail(driver.getUser().getEmail());
        }
        // CRITICAL: Include vehicleId (nullable)
        if (driver.getVehicle() != null) {
            dto.setVehicleId(driver.getVehicle().getId());
        } else {
            dto.setVehicleId(null);
        }
        dto.setCreatedAt(driver.getCreatedAt() != null ? driver.getCreatedAt().format(formatter) : "");
        return dto;
    }

    /**
     * Public method to convert Driver to DTO (used by controllers)
     */
    public DriverResponseDTO convertToDTO(Driver driver) {
        return mapToResponseDTO(driver);
    }

}
