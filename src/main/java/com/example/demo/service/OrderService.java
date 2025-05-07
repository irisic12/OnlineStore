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
        Optional<Order> orderOpt = orderRepository.findById(id);
        orderOpt.ifPresent(Order::calculateTotal);
        return orderOpt;
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomer_Id(customerId);
    }

    public List<Order> getOrdersByDateRange(Date startDate, Date endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findByIdWithItems(id) // Используем метод с загрузкой items
                .map(existingOrder -> {
                    // Сохраняем текущую сумму перед изменениями
                    BigDecimal previousTotal = existingOrder.getTotalAmount();

                    // Обновляем базовые поля
                    if (updatedOrder.getOrderDate() != null) {
                        existingOrder.setOrderDate(updatedOrder.getOrderDate());
                    }

                    if (updatedOrder.getStatus() != null) {
                        existingOrder.setStatus(updatedOrder.getStatus());
                    }

                    if (updatedOrder.getShippingAddress() != null && !updatedOrder.getShippingAddress().isEmpty()) {
                        existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
                    }

                    if (updatedOrder.getPaymentMethod() != null) {
                        existingOrder.setPaymentMethod(updatedOrder.getPaymentMethod());
                    }

                    // Обновляем клиента без сброса totalAmount
                    if (updatedOrder.getCustomer() != null && updatedOrder.getCustomer().getId() != null
                            && (existingOrder.getCustomer() == null
                            || !updatedOrder.getCustomer().getId().equals(existingOrder.getCustomer().getId()))) {
                        // Создаем новый объект Customer только с ID
                        Customer customer = new Customer();
                        customer.setId(updatedOrder.getCustomer().getId());
                        existingOrder.setCustomer(customer);
                    }

                    // Восстанавливаем original totalAmount если он не был явно задан
                    if (updatedOrder.getTotalAmount() == null) {
                        existingOrder.setTotalAmount(previousTotal);
                    } else {
                        existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
                    }

                    return orderRepository.save(existingOrder);
                })
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
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
        orderRepository.saveAndFlush(order); // Принудительное сохранение
    }

    @Transactional
    public void addItemToOrder(Long orderId, OrderItem item) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

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
}
