package com.smartfleet.service;

import com.smartfleet.dto.FuelPredictionDTO;
import com.smartfleet.entity.FuelPrediction;
import com.smartfleet.repository.FuelPredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fuel Prediction Service for managing fuel predictions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FuelPredictionService {

    private final FuelPredictionRepository fuelPredictionRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Get all fuel predictions
     */
    public List<FuelPredictionDTO> getAllPredictions() {
        return fuelPredictionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get fuel prediction by ID
     */
    public FuelPredictionDTO getPredictionById(Long id) {
        FuelPrediction prediction = fuelPredictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found: " + id));
        return mapToDTO(prediction);
    }

    /**
     * Get fuel predictions by trip ID
     */
    public List<FuelPredictionDTO> getPredictionsByTrip(Long tripId) {
        return fuelPredictionRepository.findByTripId(tripId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new fuel prediction
     */
    @Transactional
    public FuelPredictionDTO createPrediction(FuelPrediction prediction) {
        prediction = fuelPredictionRepository.save(prediction);
        log.info("Fuel prediction created: {} liters for trip {}", 
                prediction.getPredictedLiters(), prediction.getTrip().getId());
        return mapToDTO(prediction);
    }

    /**
     * Map FuelPrediction entity to DTO
     */
    private FuelPredictionDTO mapToDTO(FuelPrediction prediction) {
        FuelPredictionDTO dto = new FuelPredictionDTO();
        dto.setId(prediction.getId());
        if (prediction.getTrip() != null) {
            dto.setTripId(prediction.getTrip().getId());
        }
        dto.setPredictedLiters(prediction.getPredictedLiters());
        dto.setModelVersion(prediction.getModelVersion());
        dto.setCreatedAt(prediction.getCreatedAt() != null ? prediction.getCreatedAt().format(formatter) : "");
        return dto;
    }

}
