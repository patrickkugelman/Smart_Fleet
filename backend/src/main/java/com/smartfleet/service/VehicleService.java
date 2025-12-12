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
    private final DriverRepository driverRepository;

    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<VehicleResponseDTO> getAvailableVehicles() {
        return vehicleRepository.findByStatus("AVAILABLE").stream() // Sau "IDLE" daca asa ai in DB
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

    public VehicleResponseDTO updateVehicle(Long id, VehicleCreateDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand());
        vehicle.setType(dto.getType());
        vehicle.setStatus(dto.getStatus());

        if (dto.getLat() != null)
            vehicle.setLat(dto.getLat());
        if (dto.getLng() != null)
            vehicle.setLng(dto.getLng());

        return mapToDTO(vehicleRepository.save(vehicle));
    }

    // === METODĂ NOUĂ CRITICĂ PENTRU SIMULARE ===
    @Transactional
    public void updateLocation(Long id, Double lat, Double lng) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setLat(lat);
        vehicle.setLng(lng);
        // Putem actualiza si statusul automat daca e cazul
        if ("AVAILABLE".equals(vehicle.getStatus()) || "IDLE".equals(vehicle.getStatus())) {
            vehicle.setStatus("ON_TRIP");
        }

        vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Optional<Driver> driverOpt = driverRepository.findByVehicleId(id);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setVehicle(null);
            driverRepository.save(driver);
        }
        vehicleRepository.deleteById(id);
    }

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
        dto.setLastServiceKm(v.getLastServiceKm());
        return dto;
    }
}