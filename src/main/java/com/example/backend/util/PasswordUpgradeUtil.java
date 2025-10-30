package com.example.backend.util;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordUpgradeUtil implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Check if we should run the password upgrade
        boolean shouldUpgrade = false;
        for (String arg : args) {
            if ("--upgrade-passwords".equals(arg)) {
                shouldUpgrade = true;
                break;
            }
        }

        if (!shouldUpgrade) {
            return;
        }

        System.out.println("Starting password upgrade process...");

        // Handle users with null passwords
        List<User> nullPasswordUsers = userRepository.findByPasswordIsNull();
        System.out.println("Found " + nullPasswordUsers.size() + " users with null passwords");
        
        for (User user : nullPasswordUsers) {
            System.out.println("User with null password: " + user.getUsername() + " (" + user.getEmail() + ")");
            // For users with null passwords, we can't do much except log them
            // They would need to reset their password through a proper password reset process
        }

        // Handle users with empty passwords
        List<User> emptyPasswordUsers = userRepository.findByPasswordIsEmpty();
        System.out.println("Found " + emptyPasswordUsers.size() + " users with empty passwords");
        
        for (User user : emptyPasswordUsers) {
            System.out.println("User with empty password: " + user.getUsername() + " (" + user.getEmail() + ")");
            // For users with empty passwords, we can't do much except log them
            // They would need to reset their password through a proper password reset process
        }

        // Find all users and check for plain text passwords
        List<User> users = userRepository.findAll();
        int upgradedCount = 0;

        for (User user : users) {
            // Check for plain text passwords (not starting with $2a$) and not null/empty
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
                System.out.println("Upgrading password for user: " + user.getUsername() + " (" + user.getEmail() + ")");
                
                // Update the password with proper encoding
                userService.updatePassword(user.getUserId(), user.getPassword());
                upgradedCount++;
                
                System.out.println("Upgraded password for user: " + user.getUsername());
            }
        }

        System.out.println("Password upgrade process completed!");
        System.out.println("- Users with null passwords: " + nullPasswordUsers.size());
        System.out.println("- Users with empty passwords: " + emptyPasswordUsers.size());
        System.out.println("- Users with upgraded passwords: " + upgradedCount);
        System.exit(0);
    }
}