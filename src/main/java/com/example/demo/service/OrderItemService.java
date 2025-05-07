package com.example.demo.service;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            @Lazy OrderService orderService,
                            PlatformTransactionManager transactionManager) {
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.transactionManager = transactionManager;
    }

    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) {
        // 1. Сохраняем OrderItem
        OrderItem savedItem = orderItemRepository.save(orderItem);

        // 2. Если есть привязка к Order
        if (savedItem.getOrder() != null) {
            // 3. Создаем TransactionTemplate
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            // 4. Выполняем в новой транзакции после коммита основной
            transactionTemplate.executeWithoutResult(status -> {
                // Получаем актуальные данные заказа
                Order existingOrder = orderService.findByIdWithItems(savedItem.getOrder().getId())
                        .orElseThrow(() -> new IllegalStateException("Order not found"));

                // Создаем объект для обновления
                Order updateRequest = new Order();
                updateRequest.setId(existingOrder.getId());
                updateRequest.setOrderDate(existingOrder.getOrderDate());
                updateRequest.setStatus(existingOrder.getStatus());
                updateRequest.setShippingAddress(existingOrder.getShippingAddress());
                updateRequest.setPaymentMethod(existingOrder.getPaymentMethod());
                updateRequest.setCustomer(existingOrder.getCustomer());
                updateRequest.setTotalAmount(null); // Для пересчета

                // Вызываем updateOrder
                orderService.updateOrder(existingOrder.getId(), updateRequest);
            });
        }

        return savedItem;
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
        orderService.recalculateOrderTotal(id.getOrderId());
    }
}
