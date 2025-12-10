package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for fuel prediction response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelPredictionDTO {
    private Long id;
    private Long tripId;
    private Double predictedLiters;
    private String modelVersion;
    private String createdAt;
}
