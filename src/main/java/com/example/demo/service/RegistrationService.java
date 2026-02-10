package com.example.demo.service;

import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.entities.Customer;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.repositories.CustomerRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    @Transactional
    public User registerCustomer(RegisterRequestDTO request) {
        // Проверка уникальности логина
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // 1. Создаем User (аутентификация)
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>())
                .enabled(true)
                .build();

        user.addRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);

        // 2. Создаем Customer (бизнес-данные)
        Customer customer = Customer.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .registrationDate(LocalDate.now())
                .build();

        customerRepository.save(customer);

        return savedUser;
    }

    @Transactional
    public User registerAdmin(RegisterRequestDTO request) {
        User user = registerCustomer(request);
        user.addRole(Role.ROLE_ADMIN);
        userRepository.save(user);
        return user;
    }
}
