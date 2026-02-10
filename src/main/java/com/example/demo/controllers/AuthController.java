package com.example.demo.controllers;

import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.registerCustomer(request);
            redirectAttributes.addFlashAttribute("success",
                    "Регистрация прошла успешно! Теперь вы можете войти в систему.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("логин")) {
                bindingResult.rejectValue("username", "error.registerRequest", message);
            } else if (message.contains("email")) {
                bindingResult.rejectValue("email", "error.registerRequest", message);
            } else {
                bindingResult.reject("error.registerRequest", message);
            }
            return "register";
        }
    }
}