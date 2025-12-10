package com.smartfleet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FuelPrediction entity for AI-based fuel consumption predictions
 */
@Entity
@Table(name = "fuel_prediction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private Double predictedLiters;  // Predicted fuel consumption

    @Column(length = 50)
    private String modelVersion;  // ML model version

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
