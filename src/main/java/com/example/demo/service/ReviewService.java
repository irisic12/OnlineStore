package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.entities.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;

    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO request) {
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        // Проверяем, что заказ принадлежит текущему пользователю
        if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
            throw new IllegalArgumentException("У вас нет доступа к этому заказу");
        }

        // Проверяем статус заказа
        if (order.getStatus() != OrderStatus.DELIVERED &&
                order.getStatus() != OrderStatus.RETURNED) {
            throw new IllegalArgumentException("Отзыв можно оставить только для доставленных или возвращенных заказов");
        }

        // Проверяем, есть ли уже отзыв на этот товар в этом заказе
        if (reviewRepository.existsByOrderIdAndProductId(order.getId(), request.getProductId())) {
            throw new IllegalArgumentException("Вы уже оставили отзыв на этот товар");
        }

        // Проверяем, что товар действительно был в заказе
        boolean productInOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(request.getProductId()));

        if (!productInOrder) {
            throw new IllegalArgumentException("Этот товар не был в заказе");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден"));

        Review review = Review.builder()
                .product(product)
                .customer(currentCustomer)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .approved(false)
                .build();

        Review savedReview = reviewRepository.save(review);

        return mapToResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .filter(r -> r.getApproved())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByCurrentCustomer() {
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        return reviewRepository.findByCustomerId(currentCustomer.getId()).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByOrderId(Long orderId) {
        return reviewRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productName(review.getProduct().getName())
                .customerName(review.getCustomer().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .approved(review.getApproved())
                .build();
    }

    public boolean hasUserReviewedProductInOrder(Long orderId, Long productId) {
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        return reviewRepository.existsByOrderIdAndProductIdAndCustomerId(
                orderId, productId, currentCustomer.getId());
    }

    public Optional<ReviewResponseDTO> getUserReviewForProductInOrder(Long orderId, Long productId) {
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        return reviewRepository.findByOrderIdAndProductIdAndCustomerId(orderId, productId, currentCustomer.getId())
                .map(this::mapToResponseDTO);
    }
}