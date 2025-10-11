package com.example.backend.services;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ---------------- Create ----------------
    @CachePut(value = "users", key = "#result.userId")
    @Caching(evict = {
            @CacheEvict(value = "users", key = "'all'"),
            @CacheEvict(value = "users", key = "'username:' + #result.username"),
            @CacheEvict(value = "users", key = "'email:' + #result.email")
    })
    public User createUser(User user) {
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ---------------- Read All ----------------
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ---------------- Read By ID ----------------
    @Cacheable(value = "users", key = "#userId")
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // ---------------- Update ----------------
    @Caching(
            put = { @CachePut(value = "users", key = "#userId") },
            evict = { 
                @CacheEvict(value = "users", key = "'all'"),
                @CacheEvict(value = "users", key = "'username:' + #result.username"),
                @CacheEvict(value = "users", key = "'email:' + #result.email"),
                @CacheEvict(value = "addresses", allEntries = true) // User update may affect addresses
            }
    )
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        
        // Only encode password if it's provided and different from current
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        user.setRole(userDetails.getRole());
        return userRepository.save(user);
    }

    // ---------------- Delete ----------------
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users", key = "'all'"),
            @CacheEvict(value = "addresses", allEntries = true), // Clear all addresses as user is deleted
            @CacheEvict(value = "reviews", allEntries = true) // Clear reviews as user is deleted
    })
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    // ---------------- Find by Username ----------------
    @Cacheable(value = "users", key = "'username:' + #username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ---------------- Find by Email ----------------
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Verify password - Not cached as it's a computation, not a DB query
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}