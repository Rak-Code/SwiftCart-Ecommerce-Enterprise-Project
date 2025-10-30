package com.example.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt hashes for passwords.
 * This can be used to generate hashes for database scripts or testing.
 */
public class BCryptUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Default passwords to encode
        String[] passwords = {"temporary123", "admin123", "user123", "password123"};
        
        System.out.println("BCrypt Password Hashes:");
        System.out.println("======================");
        
        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println(password + " -> " + encoded);
            
            // Verify the encoding works
            boolean matches = encoder.matches(password, encoded);
            System.out.println("Verification: " + matches);
            System.out.println();
        }
    }
}