package com.example.backend.config;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if it doesn't exist
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Properly encoded password
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("Created default admin user with username: admin and password: admin123");
        }
        
        // Create default regular user if it doesn't exist
        if (!userRepository.findByUsername("user").isPresent()) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setEmail("user@example.com");
            regularUser.setPassword(passwordEncoder.encode("user123")); // Properly encoded password
            regularUser.setRole(User.Role.USER);
            
            userRepository.save(regularUser);
            System.out.println("Created default user with username: user and password: user123");
        }
        
        System.out.println("Data initialization completed!");
    }
}