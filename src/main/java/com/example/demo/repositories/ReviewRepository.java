package com.example.demo.repositories;

import com.example.demo.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    List<Review> findByCustomerId(Long customerId);

    Optional<Review> findByOrderIdAndProductId(Long orderId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Integer getReviewsCountByProductId(@Param("productId") Long productId);

    List<Review> findByOrderId(Long orderId);

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    boolean existsByOrderIdAndProductIdAndCustomerId(Long orderId, Long productId, Long customerId);

    Optional<Review> findByOrderIdAndProductIdAndCustomerId(Long orderId, Long productId, Long customerId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.id = :reviewId AND r.customer.id = :customerId")
    int deleteByIdAndCustomerId(@Param("reviewId") Long reviewId, @Param("customerId") Long customerId);

}