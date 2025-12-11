package com.smartfleet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {
    private Long id;
    private String name;
    private String license;
    private String status;
    private String username;
    private String email;
    private String createdAt;

    // Câmpuri CRITICE pentru tabelul de Admin
    private Long vehicleId;
    private String vehiclePlate;
    private String vehicleBrand;

    // Statistici
    private Integer tripCount;

    // Profil
    private String avatarUrl;
}