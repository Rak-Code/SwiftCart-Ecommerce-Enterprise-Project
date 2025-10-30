package com.example.backend.util;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPasswordChecker {

    @Autowired
    private UserRepository userRepository;

    /**
     * Find all users with empty or null passwords
     * @return List of users with empty/null passwords
     */
    public List<User> findUsersWithEmptyPasswords() {
        return userRepository.findByPasswordIsNull();
    }
    
    /**
     * Count users with empty or null passwords
     * @return Count of users with empty/null passwords
     */
    public long countUsersWithEmptyPasswords() {
        return userRepository.countByPasswordIsNull();
    }
}