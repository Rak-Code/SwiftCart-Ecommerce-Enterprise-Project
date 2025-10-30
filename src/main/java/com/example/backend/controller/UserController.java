package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.security.JwtUtil;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate required fields
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
            }
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
            }

            // Check if username already exists
            if (userService.findByUsername(registerRequest.getUsername().trim()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            }

            // Check if email already exists
            if (userService.findByEmail(registerRequest.getEmail().trim()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
            }

            // Create new user
            User user = User.builder()
                .username(registerRequest.getUsername().trim())
                .email(registerRequest.getEmail().trim())
                .password(registerRequest.getPassword().trim())
                .role(User.Role.USER)
                .build();

            User createdUser = userService.createUser(user);

            // Generate JWT token
            String token = jwtUtil.generateToken(
                createdUser.getEmail(),
                createdUser.getRole().toString(),
                createdUser.getUserId()
            );

            // Create response object without sensitive information
            Map<String, Object> response = new HashMap<>();
            response.put("userId", createdUser.getUserId());
            response.put("username", createdUser.getUsername());
            response.put("email", createdUser.getEmail());
            response.put("role", createdUser.getRole().toString());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Find user by email
            Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Verify password
                if (userService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                    // Generate JWT token
                    String token = jwtUtil.generateToken(
                        user.getEmail(),
                        user.getRole().toString(),
                        user.getUserId()
                    );

                    // Create response object
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", user.getUserId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole().toString());
                    response.put("token", token);

                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid credentials"));

        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Login failed"));
        }
    }
}
