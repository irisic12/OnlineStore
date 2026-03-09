package com.example.demo.controllers;

import com.example.demo.entities.Review;
import com.example.demo.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")  // ← ИЗМЕНЕНО
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public String getAllReviews(Model model) {
        List<Review> pendingReviews = reviewRepository.findByApprovedFalseOrApprovedIsNull();
        List<Review> approvedReviews = reviewRepository.findByApprovedTrue();

        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("approvedReviews", approvedReviews);

        return "reviews";  // ← ИЗМЕНЕНО (шаблон в папке admin)
    }

    @PostMapping("/approve/{id}")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            review.setApproved(true);
            reviewRepository.save(review);

            redirectAttributes.addFlashAttribute("success", "Отзыв одобрен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";  // ← ИЗМЕНЕНО
    }

    @PostMapping("/reject/{id}")
    public String rejectReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            reviewRepository.delete(review);

            redirectAttributes.addFlashAttribute("success", "Отзыв отклонен и удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";  // ← ИЗМЕНЕНО
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Отзыв удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";  // ← ИЗМЕНЕНО
    }
}