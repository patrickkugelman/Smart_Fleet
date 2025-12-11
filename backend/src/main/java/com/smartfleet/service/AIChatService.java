package com.smartfleet.service;

import com.smartfleet.entity.Driver;
import com.smartfleet.entity.Trip;
import com.smartfleet.entity.Vehicle;
import com.smartfleet.repository.DriverRepository;
import com.smartfleet.repository.TripRepository;
import com.smartfleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatService {

    private final ChatClient chatClient;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    public String generateResponse(String userQuestion) {
        // 1. Colectare Date din DB
        String context = buildSystemContext();

        // 2. Definire Prompt Sistem
        String systemText = """
                You are the AI Assistant for the Smart Fleet Tracking System.
                You have access to the current fleet data provided below in the DATA CONTEXT.
                Answer the user's question based ONLY on this data.
                If the answer is not in the data, say you don't know.
                Keep answers concise, helpful and professional.

                DATA CONTEXT:
                {context}
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(java.util.Map.of("context", context));
        Message userMessage = new UserMessage(userQuestion);

        // 3. Apelare Ollama
        try {
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            return chatClient.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Ollama error: ", e);
            return "Error: I cannot reach the AI engine. Please ensure Ollama is running locally with 'ollama run llama3'.";
        }
    }

    private String buildSystemContext() {
        StringBuilder sb = new StringBuilder();

        // Vehicule
        List<Vehicle> vehicles = vehicleRepository.findAll();
        sb.append("VEHICLES:\n");
        for (Vehicle v : vehicles) {
            sb.append(String.format("- ID:%d, Plate:%s, Brand:%s, Type:%s, Status:%s\n",
                    v.getId(), v.getPlate(), v.getBrand(), v.getType(), v.getStatus()));
        }

        // Soferi
        List<Driver> drivers = driverRepository.findAll();
        sb.append("\nDRIVERS:\n");
        for (Driver d : drivers) {
            String vehicleInfo = (d.getVehicle() != null) ? d.getVehicle().getPlate() : "None";
            sb.append(String.format("- Name:%s, Status:%s, Assigned Vehicle:%s\n",
                    d.getName(), d.getStatus(), vehicleInfo));
        }

        // Curse Recente (Ultimele 5)
        List<Trip> trips = tripRepository.findAll();
        sb.append("\nRECENT TRIPS:\n");
        int count = 0;
        for (int i = trips.size() - 1; i >= 0 && count < 5; i--) {
            Trip t = trips.get(i);
            sb.append(String.format("- From %s to %s, Dist: %.1f km, Status: %s, Driver: %s\n",
                    t.getStartLocation(), t.getEndLocation(),
                    (t.getDistance() != null ? t.getDistance() : 0.0),
                    t.getStatus(),
                    (t.getDriver() != null ? t.getDriver().getName() : "Unknown")));
            count++;
        }

        return sb.toString();
    }
}