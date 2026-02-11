package com.example.demo.service;

import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.entities.Category;
import com.example.demo.entities.Customer;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.repositories.CustomerRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public Optional<Customer> getCurrentCustomer() {
        return userService.getCurrentUser()
                .flatMap(user -> customerRepository.findByUser_Id(user.getId()));
    }

    public CustomerResponseDTO getCurrentCustomerDTO() {
        Customer customer = getCurrentCustomer()
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
        return customerMapper.toResponseDTO(customer);
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByUsername(String username) {
        return customerRepository.findByUser_Username(username);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<CustomerResponseDTO> getAllCustomersDTO() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<Customer> getCustomersByLastName(String lastName) {
        return customerRepository.findByLastName(lastName);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer updatedCustomer, String email, boolean isAdmin) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        // Обновляем поля Customer
        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        customer.setPhone(updatedCustomer.getPhone());
        customer.setAddress(updatedCustomer.getAddress());

        // Обновляем email в связанном User
        if (customer.getUser() != null && email != null && !email.isEmpty()) {
            User user = customer.getUser();

            // Проверяем уникальность email, если он меняется
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }

            user.setEmail(email);
            userRepository.save(user);
        }

        return customerRepository.save(customer);
    }

    // Для админа - полное обновление
    @Transactional
    public Customer adminUpdateCustomer(Long id, Customer updatedCustomer, String email, String username, String password) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        // Обновляем поля Customer
        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        customer.setPhone(updatedCustomer.getPhone());
        customer.setAddress(updatedCustomer.getAddress());

        // Обновляем User
        if (customer.getUser() != null) {
            User user = customer.getUser();

            // Проверяем уникальность email
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }

            // Проверяем уникальность username
            if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Пользователь с таким логином уже существует");
            }

            user.setEmail(email);
            user.setUsername(username);

            // Обновляем пароль, если он был указан
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    public List<Customer> searchCustomers(String query) {
        // Ищем по имени или фамилии
        List<Customer> byFirstName = customerRepository
                .findByFirstNameContainingIgnoreCase(query);
        List<Customer> byLastName = customerRepository
                .findByLastNameContainingIgnoreCase(query);

        // Объединяем результаты, исключая дубликаты
        return Stream.concat(byFirstName.stream(), byLastName.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public CustomerResponseDTO toResponseDTO(Customer customer) {
        return customerMapper.toResponseDTO(customer);
    }

    @Transactional
    public Customer createCustomerWithUser(Customer customer, String username, String email, String password) {
        // 1. Проверяем уникальность username и email
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // 2. Создаем User
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        user.addRole(Role.ROLE_USER);

        // 3. Создаем Customer с привязкой к User
        Customer newCustomer = Customer.builder()
                .user(user)
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .registrationDate(LocalDate.now())
                .build();

        // 4. Устанавливаем двунаправленную связь
        user.setCustomer(newCustomer);

        // 5. Сохраняем Customer - User сохранится автоматически благодаря cascade
        return customerRepository.save(newCustomer);
    }
}
