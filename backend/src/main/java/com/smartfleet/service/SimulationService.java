package com.smartfleet.service;

import com.smartfleet.entity.Trip;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.TripRepository;
import com.smartfleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Serviciu care simulează mișcarea vehiculelor și verifică starea lor.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final SimpMessagingTemplate messagingTemplate; // Pentru WebSocket
    private final Random random = new Random();

    // Rulează la fiecare 5 secunde
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void simulateFleetMovement() {
        List<Trip> activeTrips = tripRepository.findByStatus("IN_PROGRESS");

        if (activeTrips.isEmpty())
            return;

        for (Trip trip : activeTrips) {
            Vehicle vehicle = trip.getVehicle();
            if (vehicle == null)
                continue;

            // 1. Simulare Mișcare (Random Walk simplu în jurul poziției curente)
            // Într-un sistem real, am interpola între startLocation și endLocation
            double latChange = (random.nextDouble() - 0.5) * 0.002; // aprox +/- 100m
            double lngChange = (random.nextDouble() - 0.5) * 0.002;

            vehicle.setLat(vehicle.getLat() + latChange);
            vehicle.setLng(vehicle.getLng() + lngChange);

            // 2. Actualizare Kilometraj
            double distantaParcursa = 0.5; // km per tick
            vehicle.setTotalKm((vehicle.getTotalKm() != null ? vehicle.getTotalKm() : 0) + distantaParcursa);

            // 3. Verificare Geofencing (Ex: Distanță față de centru Cluj > 50km)
            // Centru Cluj: 46.7712, 23.5889
            double distFromCenter = Math
                    .sqrt(Math.pow(vehicle.getLat() - 46.7712, 2) + Math.pow(vehicle.getLng() - 23.5889, 2));
            if (distFromCenter > 0.5) { // Aprox 50km în grade (foarte simplificat)
                log.warn("🚨 GEOFENCE ALERT: Vehicle {} is out of bounds!", vehicle.getPlate());
            }

            // 4. Verificare Mentenanță
            // Dacă a mers 10.000 km de la ultima revizie
            double kmSinceService = vehicle.getTotalKm()
                    - (vehicle.getLastServiceKm() != null ? vehicle.getLastServiceKm() : 0);
            if (kmSinceService > 10000) {
                vehicle.setStatus("MAINTENANCE");
                log.warn("🔧 MAINTENANCE ALERT: Vehicle {} needs service immediately!", vehicle.getPlate());
            }

            vehicleRepository.save(vehicle);

            // 5. Trimite actualizarea prin WebSocket la Frontend (Live Map)
            // Trimitem JSON direct
            messagingTemplate.convertAndSend("/topic/vehicles", vehicle);
        }
    }
}