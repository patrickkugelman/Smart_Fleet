package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreateDTO {
    private String plate;
    private String brand;
    private String type;
    private String status;

    // Câmpuri noi necesare pentru Service
    private Double lat;
    private Double lng;
}