package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vehicle update request
 * CRITICAL: Includes brand field
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateDTO {
    private String plate;
    private String brand;  // CRITICAL: Brand field
    private String type;
    private String status;
    private Double lat;
    private Double lng;
}
