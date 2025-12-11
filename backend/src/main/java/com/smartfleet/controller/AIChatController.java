package com.smartfleet.controller;

import com.smartfleet.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAI(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("response", "Please ask a valid question."));
        }

        String response = aiChatService.generateResponse(question);
        return ResponseEntity.ok(Map.of("response", response));
    }
}