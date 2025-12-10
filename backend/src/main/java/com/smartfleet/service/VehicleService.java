package com.smartfleet.service;

import com.smartfleet.dto.VehicleCreateDTO;
import com.smartfleet.dto.VehicleUpdateDTO;
import com.smartfleet.dto.VehicleResponseDTO;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.VehicleRepository;
import com.smartfleet.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vehicle Service for CRUD operations
 * CRITICAL: Handles brand field in all operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Get all vehicles
     */
    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * CRITICAL: Get available vehicles (not assigned to any driver)
     * Used by drivers in VehicleSelectionView to choose their vehicle
     */
    public List<VehicleResponseDTO> getAvailableVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .filter(vehicle -> !driverRepository.existsByVehicleId(vehicle.getId()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle by ID
     */
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));
        return mapToResponseDTO(vehicle);
    }

    /**
     * Create new vehicle
     * CRITICAL: Includes brand field
     */
    @Transactional
    public VehicleResponseDTO createVehicle(VehicleCreateDTO dto) {
        if (vehicleRepository.existsByPlate(dto.getPlate())) {
            throw new RuntimeException("Vehicle with plate already exists: " + dto.getPlate());
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand()); // CRITICAL: Set brand
        vehicle.setType(dto.getType());
        vehicle.setStatus(dto.getStatus() != null ? dto.getStatus() : "IDLE");
        vehicle.setLat(0.0);
        vehicle.setLng(0.0);

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created: {} ({})", vehicle.getPlate(), vehicle.getBrand());

        return mapToResponseDTO(vehicle);
    }

    /**
     * Update vehicle
     * CRITICAL: Handles brand field updates
     */
    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleUpdateDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));

        if (dto.getPlate() != null && !dto.getPlate().equals(vehicle.getPlate())) {
            if (vehicleRepository.existsByPlate(dto.getPlate())) {
                throw new RuntimeException("Vehicle with plate already exists: " + dto.getPlate());
            }
            vehicle.setPlate(dto.getPlate());
        }

        if (dto.getBrand() != null) {
            vehicle.setBrand(dto.getBrand()); // CRITICAL: Update brand
        }
        if (dto.getType() != null) {
            vehicle.setType(dto.getType());
        }
        if (dto.getStatus() != null) {
            vehicle.setStatus(dto.getStatus());
        }
        if (dto.getLat() != null) {
            vehicle.setLat(dto.getLat());
        }
        if (dto.getLng() != null) {
            vehicle.setLng(dto.getLng());
        }

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated: {} ({})", vehicle.getPlate(), vehicle.getBrand());

        return mapToResponseDTO(vehicle);
    }

    /**
     * Delete vehicle
     */
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + id));
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {}", vehicle.getPlate());
    }

    /**
     * Get vehicles by status
     */
    public List<VehicleResponseDTO> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicles by type
     */
    public List<VehicleResponseDTO> getVehiclesByType(String type) {
        return vehicleRepository.findByType(type)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicles by brand
     */
    public List<VehicleResponseDTO> getVehiclesByBrand(String brand) {
        return vehicleRepository.findByBrand(brand)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search vehicles by plate
     */
    public List<VehicleResponseDTO> searchByPlate(String plate) {
        return vehicleRepository.findByPlate(plate)
                .map(vehicle -> List.of(mapToResponseDTO(vehicle)))
                .orElse(List.of());
    }

    /**
     * Update vehicle location (for real-time tracking)
     */
    @Transactional
    public void updateVehicleLocation(Long vehicleId, Double lat, Double lng) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        vehicle.setLat(lat);
        vehicle.setLng(lng);
        vehicleRepository.save(vehicle);
    }

    /**
     * Map Vehicle entity to ResponseDTO
     * CRITICAL: Includes brand field
     */
    private VehicleResponseDTO mapToResponseDTO(Vehicle vehicle) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(vehicle.getId());
        dto.setPlate(vehicle.getPlate());
        dto.setBrand(vehicle.getBrand()); // CRITICAL: Include brand
        dto.setType(vehicle.getType());
        dto.setStatus(vehicle.getStatus());
        dto.setLat(vehicle.getLat());
        dto.setLng(vehicle.getLng());
        dto.setCreatedAt(vehicle.getCreatedAt() != null ? vehicle.getCreatedAt().format(formatter) : "");
        return dto;
    }

}
