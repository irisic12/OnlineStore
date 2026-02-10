package com.example.demo.controllers;

import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        boolean isAuthenticated = userService.getCurrentUser().isPresent();
        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            userService.getCurrentUser().ifPresent(user -> {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("fullName", user.getFullName());
                model.addAttribute("isAdmin", userService.isCurrentUserAdmin());
            });
        }

        return "index";
    }
}