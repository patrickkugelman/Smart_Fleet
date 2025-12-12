package com.smartfleet.controller;

import com.smartfleet.dto.VehicleCreateDTO;
import com.smartfleet.dto.VehicleResponseDTO;
import com.smartfleet.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<VehicleResponseDTO>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleService.getAvailableVehicles());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponseDTO> createVehicle(@RequestBody VehicleCreateDTO dto) {
        return ResponseEntity.ok(vehicleService.createVehicle(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(@PathVariable Long id, @RequestBody VehicleCreateDTO dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    // === METODĂ ACTUALIZATĂ ===
    // Acum apelează service-ul și SALVEAZĂ în DB
    @PutMapping("/{id}/location")
    public ResponseEntity<Void> updateLocation(@PathVariable Long id, @RequestParam Double lat,
            @RequestParam Double lng) {

        vehicleService.updateLocation(id, lat, lng);
        return ResponseEntity.ok().build();
    }
}