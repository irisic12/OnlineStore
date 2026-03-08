package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        this.price = price;
    }

    // Вспомогательные методы для расчета рейтинга
    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .filter(r -> r.getApproved() != null && r.getApproved())
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewsCount() {
        if (reviews == null) {
            return 0;
        }
        return (int) reviews.stream()
                .filter(r -> r.getApproved() != null && r.getApproved())
                .count();
    }

}
