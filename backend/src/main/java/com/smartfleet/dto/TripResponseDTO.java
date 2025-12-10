package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for trip response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDTO {
    private Long id;
    private String driverName;
    private String vehiclePlate;
    private String startLocation;
    private String endLocation;
    private Double km;
    private String startTime;
    private String endTime;
}
