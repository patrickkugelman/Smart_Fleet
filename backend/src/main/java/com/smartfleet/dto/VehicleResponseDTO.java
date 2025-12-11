package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {
    private Long id;
    private String plate;
    private String brand;
    private String type;
    private String status;

    // Câmpuri noi adăugate pentru GPS și Kilometraj
    private Double lat;
    private Double lng;
    private Double totalKm;
    private Double lastServiceKm;
}