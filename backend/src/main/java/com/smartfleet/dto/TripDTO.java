package com.smartfleet.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String vehiclePlate;
    private String startLocation;
    private String endLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Double distance;
}