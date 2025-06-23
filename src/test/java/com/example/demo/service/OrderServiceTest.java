package com.example.demo.service;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Customer customer;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");

        Product product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setPrice(BigDecimal.valueOf(999.99));

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setId(new OrderItemId(1L, 1L));

        orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        order = new Order();
        order.setId(1L);
        order.setOrderDate(new Date());
        order.setTotalAmount(BigDecimal.valueOf(1999.98));
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress("123 Main St");
        order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        order.setCustomer(customer);
        order.setOrderItems(orderItems);
    }

    @Test
    void createOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order created = orderService.createOrder(order);

        assertNotNull(created);
        assertEquals(OrderStatus.CREATED, created.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void getOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> found = orderService.getOrderById(1L);

        assertTrue(found.isPresent());
        assertEquals(BigDecimal.valueOf(1999.98), found.get().getTotalAmount());
    }

    @Test
    void getOrdersByCustomerId() {
        when(orderRepository.findByCustomer_Id(1L)).thenReturn(Arrays.asList(order));

        List<Order> orders = orderService.getOrdersByCustomerId(1L);

        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getCustomer().getId());
    }

    @Test
    void getOrdersByDateRange() {
        Date startDate = new Date(System.currentTimeMillis() - 86400000); // yesterday
        Date endDate = new Date();

        when(orderRepository.findByOrderDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(order));

        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);

        assertEquals(1, orders.size());
    }

    @Test
    void updateOrder() {
        Order updatedOrder = new Order();
        updatedOrder.setStatus(OrderStatus.PAID);
        updatedOrder.setShippingAddress("456 Updated St");
        updatedOrder.setPaymentMethod(PaymentMethod.CASH);

        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        updatedOrder.setCustomer(newCustomer);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrder(1L, updatedOrder);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals("456 Updated St", result.getShippingAddress());
        assertEquals(PaymentMethod.CASH, result.getPaymentMethod());
        assertEquals(2L, result.getCustomer().getId());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderNotFound() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        Order updatedOrder = new Order();

        assertThrows(EntityNotFoundException.class, () -> {
            orderService.updateOrder(1L, updatedOrder);
        });
    }

    @Test
    void deleteOrder() {
        doNothing().when(orderRepository).deleteById(1L);

        orderService.deleteOrder(1L);

        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void recalculateOrderTotal() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(order);

        orderService.recalculateOrderTotal(1L);

        verify(orderRepository, times(1)).saveAndFlush(order);
        assertEquals(BigDecimal.valueOf(1999.98), order.getTotalAmount());
    }

    @Test
    void addItemToOrder() {
        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setPrice(BigDecimal.valueOf(499.99));

        OrderItem newItem = new OrderItem();
        newItem.setProduct(newProduct);
        newItem.setQuantity(3);
        newItem.setId(new OrderItemId(1L, 2L));

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(order);

        orderService.addItemToOrder(1L, newItem);

        assertEquals(2, order.getOrderItems().size());
        verify(orderRepository, times(1)).saveAndFlush(order);
    }

    @Test
    void findByIdWithItems() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        Optional<Order> found = orderService.findByIdWithItems(1L);

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getOrderItems().size());
    }

    @Test
    void removeItemFromOrder() {
        OrderItemId itemId = new OrderItemId(1L, 1L);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderItemRepository).deleteById(itemId);

        orderService.removeItemFromOrder(1L, itemId);

        verify(orderItemRepository, times(1)).deleteById(itemId);
        verify(orderRepository, times(1)).saveAndFlush(order);
    }
}
