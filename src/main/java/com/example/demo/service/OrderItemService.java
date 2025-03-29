package com.example.demo.service;

import com.example.demo.entities.OrderItem;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;

import java.util.List;
import java.util.Optional;

public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public Optional<OrderItem> getOrderItemById(OrderItemId id) {
        return orderItemRepository.findById(id);
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findById_OrderId(orderId);
    }

    public List<OrderItem> getOrderItemsByProductId(Long productId) {
        return orderItemRepository.findById_ProductId(productId);
    }

    public void deleteOrderItem(OrderItemId id) {
        orderItemRepository.deleteById(id);
    }
}
