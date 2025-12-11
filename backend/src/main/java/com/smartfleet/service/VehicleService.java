package com.smartfleet.service;

import com.smartfleet.dto.VehicleCreateDTO;
import com.smartfleet.dto.VehicleResponseDTO;
import com.smartfleet.entity.Driver;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository; // Avem nevoie de asta pentru stergere sigura

    // ... metodele existente (getAll, create, etc.) ...

    // Păstrează metodele vechi: getAllVehicles, createVehicle,
    // getAvailableVehicles...
    // Daca nu le ai la indemana, le pot rescrie, dar adaug aici doar ce e nou:

    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<VehicleResponseDTO> getAvailableVehicles() {
        return vehicleRepository.findByStatus("IDLE").stream()
                .filter(v -> !driverRepository.existsByVehicleId(v.getId()))
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public VehicleResponseDTO createVehicle(VehicleCreateDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand());
        vehicle.setType(dto.getType());
        vehicle.setStatus(dto.getStatus());
        vehicle.setLat(dto.getLat() != null ? dto.getLat() : 46.7712);
        vehicle.setLng(dto.getLng() != null ? dto.getLng() : 23.5889);

        return mapToDTO(vehicleRepository.save(vehicle));
    }

    public VehicleResponseDTO getVehicleById(Long id) {
        return vehicleRepository.findById(id).map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    // === METODE NOI (UPDATE & DELETE) ===

    public VehicleResponseDTO updateVehicle(Long id, VehicleCreateDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand());
        vehicle.setType(dto.getType());
        vehicle.setStatus(dto.getStatus());
        // Nu actualizam locatia aici de obicei, dar se poate daca e nevoie

        return mapToDTO(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        // 1. Gasim daca vreun sofer are masina asta
        Optional<Driver> driverOpt = driverRepository.findByVehicleId(id);

        // 2. Daca da, il dam jos din masina (Vehicle = null)
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setVehicle(null);
            driverRepository.save(driver);
        }

        // 3. Stergem vehiculul
        vehicleRepository.deleteById(id);
    }

    // Helper
    private VehicleResponseDTO mapToDTO(Vehicle v) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(v.getId());
        dto.setPlate(v.getPlate());
        dto.setBrand(v.getBrand());
        dto.setType(v.getType());
        dto.setStatus(v.getStatus());
        dto.setLat(v.getLat());
        dto.setLng(v.getLng());
        dto.setTotalKm(v.getTotalKm());
        return dto;
    }

    // ... restul metodelor de search/filter daca le ai ...
    public List<VehicleResponseDTO> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status).stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}