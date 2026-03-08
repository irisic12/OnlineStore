package com.example.demo.controllers;

import com.example.demo.dto.ReviewRequestDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Review;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/product/{productId}")
    public String getProductReviews(@PathVariable Long productId, Model model) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByProductId(productId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("productId", productId);
        return "product-reviews";
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER')")
    public String getMyReviews(Model model) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByCurrentCustomer();
        model.addAttribute("reviews", reviews);
        return "my-reviews";
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public String getOrderReviews(@PathVariable Long orderId, Model model) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByOrderId(orderId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("orderId", orderId);
        return "order-reviews";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public String addReview(@Valid @ModelAttribute ReviewRequestDTO request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Проверьте правильность заполнения формы");
            return "redirect:/reviews/add-form?productId=" + request.getProductId() + "&orderId=" + request.getOrderId();
        }

        try {
            reviewService.createReview(request);
            redirectAttributes.addFlashAttribute("success", "Отзыв успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/add-form?productId=" + request.getProductId() + "&orderId=" + request.getOrderId();
        }
        return "redirect:/user/orders/" + request.getOrderId() + "/items";
    }

    @GetMapping("/add-form")
    @PreAuthorize("hasRole('USER')")
    public String showReviewForm(@RequestParam Long productId,
                                 @RequestParam Long orderId,
                                 Model model) {
        model.addAttribute("productId", productId);
        model.addAttribute("orderId", orderId);
        return "review-form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String showEditReviewForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            if (!review.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому отзыву");
                return "redirect:/user/orders";
            }

            model.addAttribute("review", review);
            model.addAttribute("productId", review.getProduct().getId());
            model.addAttribute("orderId", review.getOrder().getId());

            return "review-edit-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('USER')")
    public String updateReview(@PathVariable Long id,
                               @Valid @ModelAttribute ReviewRequestDTO request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Проверьте правильность заполнения формы");
            return "redirect:/reviews/edit/" + id;
        }

        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            if (!review.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому отзыву");
                return "redirect:/user/orders";
            }

            review.setRating(request.getRating());
            review.setComment(request.getComment());
            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("success", "Отзыв успешно обновлен");
            return "redirect:/user/orders/" + review.getOrder().getId() + "/items";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            if (!review.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому отзыву");
                return "redirect:/user/orders";
            }

            Long orderId = review.getOrder().getId();
            reviewRepository.delete(review);

            redirectAttributes.addFlashAttribute("success", "Отзыв успешно удален");
            return "redirect:/user/orders/" + orderId + "/items";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasRole('USER')")
    public String viewReview(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            // Проверяем, что отзыв принадлежит текущему пользователю
            if (!review.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому отзыву");
                return "redirect:/user/orders";
            }

            model.addAttribute("review", review);
            return "review-view";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }
}