package com.smartfleet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vehicle entity representing vehicles in the fleet
 * CRITICAL: Includes brand field for complete vehicle information
 */
@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String plate;  // License plate

    @Column(length = 50)
    private String brand;  // CRITICAL: Vehicle brand (Toyota, Volvo, etc.)

    @Column(nullable = false, length = 50)
    private String type;  // Vehicle type (Truck, Sedan, Van, etc.)

    @Column(length = 20)
    private String status;  // ACTIVE, IDLE, MAINTENANCE

    private Double lat;  // Current latitude
    private Double lng;  // Current longitude

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "IDLE";
        }
    }

}
