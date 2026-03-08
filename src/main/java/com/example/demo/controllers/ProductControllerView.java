package com.example.demo.controllers;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.Review;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductControllerView {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    public ProductControllerView(ProductService productService,
                                 CategoryService categoryService,
                                 UserService userService,
                                 ReviewRepository reviewRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String getAllProducts(@RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String search,
                                 Model model) {

        // Получаем текущего пользователя и его роль
        boolean isAdmin = userService.getCurrentUser()
                .map(user -> user.hasRole(com.example.demo.enums.Role.ROLE_ADMIN))
                .orElse(false);

        List<Product> products;

        // Применяем фильтры
        if (search != null && !search.isEmpty()) {
            products = productService.searchProductsByName(search);
        } else if (categoryId != null) {
            products = productService.getProductsByCategoryId(categoryId);
        } else {
            products = productService.getAllProducts();
        }

        // Для каждого товара загружаем отзывы
        Map<Long, List<Review>> reviewsMap = new HashMap<>();
        Map<Long, Double> ratingMap = new HashMap<>();

        for (Product product : products) {
            List<Review> reviews = reviewRepository.findByProductId(product.getId());
            reviewsMap.put(product.getId(), reviews);
            ratingMap.put(product.getId(), product.getAverageRating());
        }

        model.addAttribute("products", products);
        model.addAttribute("reviewsMap", reviewsMap);
        model.addAttribute("ratingMap", ratingMap);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("isAdmin", isAdmin);  // ← передаём роль в шаблон

        return "products";
    }

    @GetMapping("/{id}/reviews")
    public String getProductReviews(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        List<Review> reviews = reviewRepository.findByProductId(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);

        return "product-reviews";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product-form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.createProduct(product);
        redirectAttributes.addFlashAttribute("success", "Товар успешно добавлен");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id).orElse(null));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product-form";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.updateProduct(id, product);
        redirectAttributes.addFlashAttribute("success", "Товар успешно обновлен");
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Товар успешно удален");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Невозможно удалить товар, так как с ним связаны заказы");
        }
        return "redirect:/products";
    }
}