package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for driver response
 * CRITICAL: Includes vehicleId for vehicle assignment tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {
    private Long id;
    private String name;
    private String license;
    private String status;
    private String username;
    private String email;
    private String createdAt;
    private Long vehicleId; // CRITICAL: Nullable - vehicle assignment status
}
