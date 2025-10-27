package com.example.backend.controller;

import com.example.backend.model.ChatRequest;
import com.example.backend.model.ChatResponse;
import com.example.backend.services.GroqChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173") // React frontend URL
public class ChatController {

    private final GroqChatService groqChatService;

    @Autowired
    public ChatController(GroqChatService groqChatService) {
        this.groqChatService = groqChatService;
    }

    @PostMapping
    public ChatResponse processMessage(@RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return new ChatResponse("Please provide a message.");
        }

        String response = groqChatService.generateResponse(request.getMessage());
        return new ChatResponse(response);
    }
}
