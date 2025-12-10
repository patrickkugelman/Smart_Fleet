package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a vehicle to a driver
 * CRITICAL: Used by drivers to select their vehicle
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignVehicleDTO {
    private Long vehicleId;
}
