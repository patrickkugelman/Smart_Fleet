package com.smartfleet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Trip entity representing a journey by a driver
 */
@Entity
@Table(name = "trip")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(length = 255)
    private String startLocation;

    @Column(length = 255)
    private String endLocation;

    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;

    @Column(name = "km")
    private Double km; // Distance covered (old field)

    @Column(name = "distance")
    private Double distance; // Distance (new field for compatibility)

    @Column(name = "status", length = 50)
    private String status; // Trip status: ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "ASSIGNED";
        }
        // Sync km and distance fields
        if (distance != null && km == null) {
            km = distance;
        }
        if (km != null && distance == null) {
            distance = km;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Sync km and distance fields on update
        if (distance != null && km == null) {
            km = distance;
        }
        if (km != null && distance == null) {
            distance = km;
        }
    }

}