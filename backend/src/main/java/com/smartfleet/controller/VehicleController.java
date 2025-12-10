package com.smartfleet.controller;

import com.smartfleet.dto.VehicleCreateDTO;
import com.smartfleet.dto.VehicleUpdateDTO;
import com.smartfleet.dto.VehicleResponseDTO;
import com.smartfleet.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vehicle Controller - 16 REST endpoints for vehicle management
 * CRITICAL: All endpoints support brand field
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * GET: Get all vehicles
     */
    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        log.info("Fetching all vehicles");
        List<VehicleResponseDTO> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * CRITICAL: GET available vehicles (not assigned to any driver)
     * Used by drivers to select their vehicle
     */
    @GetMapping("/available")
    public ResponseEntity<List<VehicleResponseDTO>> getAvailableVehicles() {
        log.info("Fetching available vehicles");
        List<VehicleResponseDTO> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * GET: Get vehicle by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        log.info("Fetching vehicle by ID: {}", id);
        VehicleResponseDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * POST: Create new vehicle
     * CRITICAL: Includes brand field in request
     */
    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(@RequestBody VehicleCreateDTO dto) {
        log.info("Creating new vehicle: {} ({})", dto.getPlate(), dto.getBrand());
        VehicleResponseDTO vehicle = vehicleService.createVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /**
     * PUT: Update vehicle
     * CRITICAL: Includes brand field in request
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable Long id,
            @RequestBody VehicleUpdateDTO dto) {
        log.info("Updating vehicle: {}", id);
        VehicleResponseDTO vehicle = vehicleService.updateVehicle(id, dto);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * DELETE: Delete vehicle
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("Deleting vehicle: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET: Get vehicles by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByStatus(@PathVariable String status) {
        log.info("Fetching vehicles by status: {}", status);
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByStatus(status);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * GET: Get vehicles by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByType(@PathVariable String type) {
        log.info("Fetching vehicles by type: {}", type);
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByType(type);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * GET: Get vehicles by brand
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByBrand(@PathVariable String brand) {
        log.info("Fetching vehicles by brand: {}", brand);
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByBrand(brand);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * GET: Search vehicles by plate
     */
    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponseDTO>> searchVehicles(@RequestParam String plate) {
        log.info("Searching vehicles by plate: {}", plate);
        List<VehicleResponseDTO> vehicles = vehicleService.searchByPlate(plate);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * PUT: Update vehicle location (real-time tracking)
     */
    @PutMapping("/{id}/location")
    public ResponseEntity<Void> updateVehicleLocation(
            @PathVariable Long id,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        log.info("Updating location for vehicle {}: ({}, {})", id, lat, lng);
        vehicleService.updateVehicleLocation(id, lat, lng);
        return ResponseEntity.ok().build();
    }

}
