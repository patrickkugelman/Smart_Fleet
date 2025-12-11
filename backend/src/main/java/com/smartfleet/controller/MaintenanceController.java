package com.smartfleet.controller;

import com.smartfleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final VehicleRepository vehicleRepository;

    @PostMapping("/service/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> performService(@PathVariable Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    // Resetăm contorul de mentenanță
                    vehicle.setLastServiceKm(vehicle.getTotalKm());

                    // Dacă era în mentenanță, o punem înapoi IDLE
                    if ("MAINTENANCE".equals(vehicle.getStatus())) {
                        vehicle.setStatus("IDLE");
                    }

                    vehicleRepository.save(vehicle);
                    return ResponseEntity.ok("Service performed successfully for " + vehicle.getPlate());
                })
                .orElse(ResponseEntity.notFound().build());
    }
}