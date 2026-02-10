package com.example.demo.mapper;

import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.entities.Customer;
import com.example.demo.entities.User;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CustomerMapper {

    public Customer toEntity(RegisterRequestDTO dto) {
        Customer customer = Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .registrationDate(LocalDate.now())
                .build();

        return customer;
    }

    public CustomerResponseDTO toResponseDTO(Customer customer) {
        User user = customer.getUser();

        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .loyaltyCard(customer.getLoyaltyCard())
                .totalSpent(customer.getTotalSpent())
                .registrationDate(customer.getRegistrationDate())
                .birthDate(customer.getBirthDate())
                .orderCount(customer.getOrders() != null ?
                        customer.getOrders().size() : 0)
                .build();
    }

    public User createUserFromRequest(RegisterRequestDTO dto, String encodedPassword) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(encodedPassword)
                .build();
    }
}
