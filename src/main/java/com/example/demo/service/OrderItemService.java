package com.example.demo.service;

import com.example.demo.entities.OrderItem;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            OrderService orderService) {
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        OrderItem saved = orderItemRepository.save(orderItem);
        orderService.recalculateOrderTotal(orderItem.getOrder().getId());
        return saved;
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
        Long orderId = id.getOrderId();
        orderItemRepository.deleteById(id);
        orderService.recalculateOrderTotal(orderId);
    }
}
