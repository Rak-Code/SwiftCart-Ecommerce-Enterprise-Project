package com.example.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqChatService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-20b}")  // change default to a Groq-compatible model
    private String model;

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";  // or /v1/completions depending on model

    public String generateResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Using messages format as per OpenAI-style API
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        List<Map<String,String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userMessage));
        payload.put("messages", messages);
        // optional tuning
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 3000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(API_URL, request, Map.class);

            if (response == null || response.isEmpty()) {
                return "No response from Groq API.";
            }

            // parse choices -> choices[0].message.content
            if (response.containsKey("choices")) {
                Object choicesObj = response.get("choices");
                if (choicesObj instanceof List) {
                    List<?> choices = (List<?>) choicesObj;
                    if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                        Map<?,?> firstChoice = (Map<?,?>) choices.get(0);
                        Object messageObj = firstChoice.get("message");
                        if (messageObj instanceof Map) {
                            Object content = ((Map<?,?>) messageObj).get("content");
                            if (content instanceof String) {
                                return (String) content;
                            }
                        }
                        // fallback to "text"
                        if (firstChoice.containsKey("text")) {
                            return String.valueOf(firstChoice.get("text"));
                        }
                    }
                }
            }

            // fallback: return full response
            return response.toString();

        } catch (Exception e) {
            return "Error calling Groq API: " + e.getMessage();
        }
    }
}
