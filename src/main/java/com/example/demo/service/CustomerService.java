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
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setFirstName(updatedCustomer.getFirstName());
                    customer.setLastName(updatedCustomer.getLastName());
                    customer.setPhone(updatedCustomer.getPhone());
                    customer.setAddress(updatedCustomer.getAddress());
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
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
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User savedUser = userRepository.save(user);

        // 3. Создаем Customer с привязкой к User
        Customer newCustomer = Customer.builder()
                .user(savedUser)
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .registrationDate(LocalDate.now())
                .build();

        return customerRepository.save(newCustomer);
    }
}
