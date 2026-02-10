package com.example.demo.service;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username);
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.hasRole(Role.ROLE_ADMIN))
                .orElse(false);
    }

    public boolean isCurrentUserCustomer() {
        return getCurrentUser()
                .map(user -> user.hasRole(Role.ROLE_USER))
                .orElse(false);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
