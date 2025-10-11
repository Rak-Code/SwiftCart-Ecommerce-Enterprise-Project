package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.security.JwtUtil;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest) {
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
            if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            }

            // Check if email already exists
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername().trim());
            user.setEmail(registerRequest.getEmail().trim());
            user.setPassword(registerRequest.getPassword().trim());
            
            // Set role from request if provided, otherwise default to USER
            if (registerRequest.getRole() != null) {
                try {
                    user.setRole(User.Role.valueOf(registerRequest.getRole().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Invalid role: " + registerRequest.getRole()));
                }
            } else {

                // Set role from request if provided, otherwise default to USER
                if (registerRequest.getRole() != null && !registerRequest.getRole().trim().isEmpty()) {
                    try {
                        user.setRole(User.Role.valueOf(registerRequest.getRole().trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Invalid role: " + registerRequest.getRole()));
                    }
                } else {
                    user.setRole(User.Role.USER);
                }
            }

            User createdUser = userService.createUser(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                createdUser.getEmail(), 
                createdUser.getRole().toString(), 
                createdUser.getUserId()
            );

            // Create a response object without sensitive information
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        // Check if user is accessing their own data or is admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        Optional<User> requestedUser = userService.getUserById(userId);

        if (requestedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Allow if user is admin or accessing their own data
        if (currentUser.isPresent() && 
            (currentUser.get().getRole() == User.Role.ADMIN || 
             currentUser.get().getUserId().equals(userId))) {
            return ResponseEntity.ok(requestedUser.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Access denied"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                           @RequestBody User userDetails) {
        // Check if user is updating their own data or is admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        Optional<User> currentUser = userService.findByEmail(currentUserEmail);

        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Unauthorized"));
        }

        // Allow if user is admin or updating their own data
        if (currentUser.get().getRole() == User.Role.ADMIN || 
            currentUser.get().getUserId().equals(userId)) {
            User updatedUser = userService.updateUser(userId, userDetails);
            return ResponseEntity.ok(updatedUser);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Access denied"));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> findByUsername(@PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        Optional<User> requestedUser = userService.findByUsername(username);

        if (requestedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Allow if user is admin or accessing their own data
        if (currentUser.isPresent() && 
            (currentUser.get().getRole() == User.Role.ADMIN || 
             currentUser.get().getUsername().equals(username))) {
            return ResponseEntity.ok(requestedUser.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Access denied"));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        Optional<User> requestedUser = userService.findByEmail(email);

        if (requestedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Allow if user is admin or accessing their own data
        if (currentUser.isPresent() && 
            (currentUser.get().getRole() == User.Role.ADMIN || 
             currentUser.get().getEmail().equals(email))) {
            return ResponseEntity.ok(requestedUser.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Access denied"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            logger.debug("Login attempt for email: {}", loginRequest.getEmail());
            
            // Find user by email
            Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Verify password using BCrypt
                if (userService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                    // Generate JWT token
                    String token = jwtUtil.generateToken(
                        user.getEmail(), 
                        user.getRole().toString(), 
                        user.getUserId()
                    );

                    // Create a simplified response object
                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", user.getUserId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole().toString());
                    response.put("token", token);
                    
                    logger.debug("Login successful for user: {}", user.getEmail());
                    return ResponseEntity.ok(response);
                }
            }
            
            logger.debug("Login failed: Invalid credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid credentials"));
                
        } catch (Exception e) {
            logger.error("Login error for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An error occurred during login"));
        }
    }
}