package com.example.demo.repositories;

import com.example.demo.entities.OrderItem;
import com.example.demo.helpClass.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    // Найти все OrderItem для определенного заказа
    List<OrderItem> findById_OrderId(Long orderId);

    // Найти все OrderItem для определенного продукта
    List<OrderItem> findById_ProductId(Long productId);

    void deleteById(OrderItemId id);
}
