package com.smartfleet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String plate;

    @Column(length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 20)
    private String status;

    private Double lat;
    private Double lng;

    // --- CÂMPURI NOI PENTRU MENTENANȚĂ & SIMULARE ---
    @Column(name = "total_km")
    private Double totalKm; // Kilometraj total real

    @Column(name = "last_service_km")
    private Double lastServiceKm; // La ce km a fost ultima revizie

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (status == null)
            status = "IDLE";
        if (totalKm == null)
            totalKm = 0.0;
        if (lastServiceKm == null)
            lastServiceKm = 0.0;

        // Coordonate default (Cluj) dacă lipsesc
        if (lat == null)
            lat = 46.7712;
        if (lng == null)
            lng = 23.5889;
    }
}