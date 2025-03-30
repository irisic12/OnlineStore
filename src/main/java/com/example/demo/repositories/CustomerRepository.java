package com.example.demo.repositories;

import com.example.demo.entities.Category;
import com.example.demo.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Найти клиентов по фамилии
    List<Customer> findByLastName(String lastName);

    // Найти клиентов по email
    List<Customer> findByEmail(String email);
}
