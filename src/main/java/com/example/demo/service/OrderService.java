package com.example.demo.service;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;

    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
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

    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setOrderDate(updatedOrder.getOrderDate());
                    order.setTotalAmount(updatedOrder.getTotalAmount());
                    order.setStatus(updatedOrder.getStatus());
                    order.setShippingAddress(updatedOrder.getShippingAddress());
                    return orderRepository.save(order);
                })
                .orElse(null);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
