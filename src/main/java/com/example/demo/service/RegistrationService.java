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

        // 1. Создаем User (без сохранения)
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user.addRole(Role.ROLE_USER);

        // 2. Создаем Customer с привязкой к User
        Customer customer = Customer.builder()
                .user(user)  // Устанавливаем связь
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .registrationDate(LocalDate.now())
                .build();

        // 3. Устанавливаем двунаправленную связь
        user.setCustomer(customer);

        // 4. Сохраняем Customer - User сохранится автоматически благодаря cascade
        customerRepository.save(customer);

        return user;
    }

    @Transactional
    public User registerAdmin(RegisterRequestDTO request) {
        User user = registerCustomer(request);
        user.addRole(Role.ROLE_ADMIN);
        userRepository.save(user); // Обновляем роли
        return user;
    }
}
