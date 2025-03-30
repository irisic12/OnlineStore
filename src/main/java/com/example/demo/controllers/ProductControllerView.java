package com.example.demo.controllers;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductControllerView {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductControllerView(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product-form";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product) {
        productService.createProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id).orElse(null));
        model.addAttribute("categories", categoryService.searchCategoriesByName(""));
        return "product-form";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
        productService.updateProduct(id, product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}