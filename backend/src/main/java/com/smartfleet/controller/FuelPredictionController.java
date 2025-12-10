package com.smartfleet.controller;

import com.smartfleet.dto.FuelPredictionDTO;
import com.smartfleet.service.FuelPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fuel Prediction Controller for fuel analytics
 */
@RestController
@RequestMapping("/api/fuel-predictions")
@RequiredArgsConstructor
@Slf4j
public class FuelPredictionController {

    private final FuelPredictionService fuelPredictionService;

    /**
     * GET: Get all fuel predictions
     */
    @GetMapping
    public ResponseEntity<List<FuelPredictionDTO>> getAllPredictions() {
        log.info("Fetching all fuel predictions");
        List<FuelPredictionDTO> predictions = fuelPredictionService.getAllPredictions();
        return ResponseEntity.ok(predictions);
    }

    /**
     * GET: Get prediction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FuelPredictionDTO> getPredictionById(@PathVariable Long id) {
        log.info("Fetching fuel prediction by ID: {}", id);
        FuelPredictionDTO prediction = fuelPredictionService.getPredictionById(id);
        return ResponseEntity.ok(prediction);
    }

    /**
     * GET: Get predictions by trip ID
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<FuelPredictionDTO>> getPredictionsByTrip(@PathVariable Long tripId) {
        log.info("Fetching fuel predictions for trip: {}", tripId);
        List<FuelPredictionDTO> predictions = fuelPredictionService.getPredictionsByTrip(tripId);
        return ResponseEntity.ok(predictions);
    }

}
