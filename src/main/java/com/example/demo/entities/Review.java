package com.example.demo.entities;

import com.example.demo.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer rating; // оценка от 1 до 5

    @Column(length = 1000)
    private String comment;

    @Column(name = "is_approved")
    private Boolean approved; // для модерации отзывов (опционально)

    // Вспомогательный метод для проверки, может ли пользователь оставить отзыв
    public boolean canLeaveReview() {
        return order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.RETURNED;
    }
}