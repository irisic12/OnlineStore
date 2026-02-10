package com.example.demo.controllers;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.entities.Customer;
import com.example.demo.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CustomerControllerView {

    private final CustomerService customerService;

    // 1. Просмотр всех клиентов
    @GetMapping
    public String getAllCustomers(Model model) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers";
    }

    // 2. Форма добавления нового клиента
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customerRequest", new CustomerRequestDTO());
        return "customer-form";
    }

    // 3. Добавление клиента с пользователем
    @PostMapping("/add")
    public String addCustomer(
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "customer-form";
        }

        try {
            // Создаем Customer из данных формы
            Customer customer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build();

            // Создаем Customer и User вместе
            customerService.createCustomerWithUser(
                    customer,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            return "redirect:/customers";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            return "customer-form";
        }
    }

    // 4. Форма редактирования клиента (только Customer, без User)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        customerService.getCustomerById(id).ifPresent(customer -> {
            CustomerRequestDTO request = CustomerRequestDTO.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .email(customer.getUser() != null ? customer.getUser().getEmail() : "")
                    .phone(customer.getPhone())
                    .address(customer.getAddress())
                    .build();
            model.addAttribute("customerRequest", request);
        });
        return "customer-form";
    }

    // 5. Обновление клиента (только данные Customer)
    @PostMapping("/update/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "customer-form";
        }

        try {
            Customer updatedCustomer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build();

            customerService.updateCustomer(id, updatedCustomer);
            return "redirect:/customers";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            return "customer-form";
        }
    }

    // 6. Удаление клиента
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }
}