package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.services.UserService;
import com.example.backend.util.UserPasswordChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserPasswordChecker userPasswordChecker;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get users with empty passwords
     */
    @GetMapping("/users/empty-passwords")
    public ResponseEntity<Map<String, Object>> getUsersWithEmptyPasswords() {
        List<User> users = userPasswordChecker.findUsersWithEmptyPasswords();
        long count = userPasswordChecker.countUsersWithEmptyPasswords();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("users", users);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Set password for a user (admin only)
     */
    @PostMapping("/users/{userId}/set-password")
    public ResponseEntity<Map<String, Object>> setUserPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        String newPassword = request.get("password");
        
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Password cannot be empty"));
        }
        
        try {
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Encode and set the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(userId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "Error updating password: " + e.getMessage()));
        }
    }
}