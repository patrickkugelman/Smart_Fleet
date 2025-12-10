package com.smartfleet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Driver entity representing drivers in the fleet
 * CRITICAL: vehicle_id is nullable - drivers can have no vehicle assigned
 */
@Entity
@Table(name = "driver")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String license; // Driver's license number

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle; // CRITICAL: Nullable - vehicle assignment is optional

    @Column(length = 20)
    private String status; // AVAILABLE, ON_TRIP, OFF_DUTY, ACTIVE, INACTIVE, SUSPENDED, ON_LEAVE

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "AVAILABLE";
        }
    }

}
