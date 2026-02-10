package com.example.demo.controllers.api;

import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.entities.Customer;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerControllerApi {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponseDTO> getCurrentCustomer() {
        return ResponseEntity.ok(customerService.getCurrentCustomerDTO());
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomersDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(customer -> ResponseEntity.ok(customerService.toResponseDTO(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByUsername(@PathVariable String username) {
        return customerService.getCustomerByUsername(username)
                .map(customer -> ResponseEntity.ok(customerService.toResponseDTO(customer)))
                .orElse(ResponseEntity.notFound().build());
    }
}
