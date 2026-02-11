package com.example.demo.controllers;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.entities.Customer;
import com.example.demo.service.CustomerService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CustomerControllerView {

    private final CustomerService customerService;
    private final UserService userService;

    @GetMapping
    public String getAllCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customerRequest", new CustomerRequestDTO());
        model.addAttribute("isAdmin", true);
        model.addAttribute("actionUrl", "/customers/add");
        model.addAttribute("cancelUrl", "/customers");
        model.addAttribute("isNewUser", true);
        model.addAttribute("showPassword", true);
        return "customer-form";
    }

    @PostMapping("/add")
    public String addCustomer(
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("actionUrl", "/customers/add");
            model.addAttribute("cancelUrl", "/customers");
            model.addAttribute("isNewUser", true);
            return "customer-form";
        }

        try {
            Customer customer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build();

            customerService.createCustomerWithUser(
                    customer,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );

            redirectAttributes.addFlashAttribute("success", "Клиент успешно добавлен");
            return "redirect:/customers";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            model.addAttribute("isAdmin", true);
            model.addAttribute("actionUrl", "/customers/add");
            model.addAttribute("cancelUrl", "/customers");
            model.addAttribute("isNewUser", true);
            return "customer-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        customerService.getCustomerById(id).ifPresent(customer -> {
            CustomerRequestDTO request = CustomerRequestDTO.builder()
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .email(customer.getUser() != null ? customer.getUser().getEmail() : "")
                    .username(customer.getUser() != null ? customer.getUser().getUsername() : "")
                    .phone(customer.getPhone())
                    .address(customer.getAddress())
                    .build();

            model.addAttribute("customerRequest", request);
            model.addAttribute("isAdmin", true);
            model.addAttribute("actionUrl", "/customers/update/" + customer.getId());
            model.addAttribute("cancelUrl", "/customers");
            model.addAttribute("isNewUser", false);
        });
        return "customer-form";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("actionUrl", "/customers/update/" + id);
            model.addAttribute("cancelUrl", "/customers");
            model.addAttribute("isNewUser", false);
            return "customer-form";
        }

        try {
            Customer customer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build();

            customerService.adminUpdateCustomer(
                    id,
                    customer,
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword()
            );

            redirectAttributes.addFlashAttribute("success", "Клиент успешно обновлен");
            return "redirect:/customers";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            model.addAttribute("isAdmin", true);
            model.addAttribute("actionUrl", "/customers/update/" + id);
            model.addAttribute("cancelUrl", "/customers");
            model.addAttribute("isNewUser", false);
            return "customer-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Клиент удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления: " + e.getMessage());
        }
        return "redirect:/customers";
    }
}