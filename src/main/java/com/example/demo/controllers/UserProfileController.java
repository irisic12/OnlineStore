package com.example.demo.controllers;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.entities.Customer;
import com.example.demo.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String getUserProfile(Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            CustomerRequestDTO customerRequest = CustomerRequestDTO.builder()
                    .id(currentCustomer.getId())
                    .firstName(currentCustomer.getFirstName())
                    .lastName(currentCustomer.getLastName())
                    .email(currentCustomer.getUser() != null ? currentCustomer.getUser().getEmail() : "")
                    .phone(currentCustomer.getPhone())
                    .address(currentCustomer.getAddress())
                    .build();

            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("isNewUser", false);

            return "customer-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки профиля: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('USER')")
    public String updateUserProfile(
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
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

            customerService.updateCustomer(
                    request.getId(),
                    customer,
                    request.getEmail(),
                    false
            );

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("isNewUser", false);
            return "customer-form";
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public String getUserOrders(Model model) {
        return "user/orders";
    }
}