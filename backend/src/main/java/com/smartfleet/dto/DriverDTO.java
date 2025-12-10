package com.smartfleet.dto;

import lombok.Data;

@Data
public class DriverDTO {
    private Long id;
    private String name;
    private String license;
    private String status;
    private Long userId;
    private String username;
    private String email;
    private Long vehicleId;
    private String vehiclePlate;
    private String vehicleBrand;
    private Integer tripCount;
}