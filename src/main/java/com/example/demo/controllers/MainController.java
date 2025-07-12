package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // Главная страница (до входа)
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Страница после входа
    @GetMapping("/home")
    public String dashboard() {
        return "dashboard";
    }
}