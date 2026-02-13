package com.example.demo.service;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        @Lazy OrderItemService orderItemService,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order createOrder(Order order) {
        order.setTotalAmount(BigDecimal.ZERO);
        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
        // Убираем calculateTotal() отсюда, чтобы не вызывать его лишний раз
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomer_Id(customerId);
    }

    public List<Order> getOrdersByDateRange(Date startDate, Date endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    // Обновляем только поля, которые могли измениться в форме
                    existingOrder.setOrderDate(updatedOrder.getOrderDate());
                    existingOrder.setStatus(updatedOrder.getStatus());
                    existingOrder.setPaymentMethod(updatedOrder.getPaymentMethod());
                    existingOrder.setShippingAddress(updatedOrder.getShippingAddress());

                    // Обновляем клиента
                    if (updatedOrder.getCustomer() != null) {
                        existingOrder.setCustomer(updatedOrder.getCustomer());
                    }

                    // ВАЖНО: НЕ трогаем orderItems и totalAmount - они остаются как есть
                    // totalAmount пересчитается отдельным методом

                    return orderRepository.save(existingOrder);
                })
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        // Удаляем все OrderItem связанные с заказом
        order.getOrderItems().clear(); // Это удалит все позиции благодаря orphanRemoval=true

        // Удаляем сам заказ
        orderRepository.delete(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void recalculateOrderTotal(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        orderRepository.saveAndFlush(order);
    }

    @Transactional
    public void addItemToOrder(Long orderId, OrderItem item) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        // Проверяем, есть ли уже такой товар в заказе
        boolean productExists = order.getOrderItems().stream()
                .anyMatch(existingItem -> existingItem.getProduct().getId().equals(item.getProduct().getId()));

        if (productExists) {
            throw new IllegalArgumentException("Товар \"" + item.getProduct().getName() + "\" уже есть в заказе");
        }

        order.getOrderItems().add(item);
        item.setOrder(order);

        // Явно вызываем пересчёт
        order.calculateTotal();
        orderRepository.saveAndFlush(order);
    }

    @Transactional
    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    @Transactional
    public void removeItemFromOrder(Long orderId, OrderItemId itemId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        // Находим и удаляем item
        order.getOrderItems().removeIf(item -> item.getId().equals(itemId));

        // Пересчитываем сумму
        order.calculateTotal();
        orderRepository.saveAndFlush(order);

        // Явно удаляем из БД
        orderItemRepository.deleteById(itemId);
    }

    @Transactional
    public void deleteOrderWithItems(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        // Благодаря cascade = CascadeType.ALL и orphanRemoval = true,
        // все OrderItem удалятся автоматически при удалении заказа
        orderRepository.delete(order);
    }
}
