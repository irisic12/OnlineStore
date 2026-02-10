package com.example.demo.controllers;

import org.springframework.ui.Model;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String getUserProfile(Model model) {
        model.addAttribute("customer", customerService.getCurrentCustomerDTO());
        return "user/profile";
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public String getUserOrders(Model model) {
        // Здесь нужно получить заказы текущего пользователя
        // Пока заглушка
        return "user/orders";
    }
}

