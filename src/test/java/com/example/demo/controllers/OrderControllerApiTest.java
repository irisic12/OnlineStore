package com.example.demo.controllers;

import com.example.demo.controllers.api.OrderControllerApi;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerApiTest {

    private MockMvc mockMvc;
    private OrderService orderService;
    private OrderControllerApi controller;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        controller = new OrderControllerApi(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createOrder() throws Exception {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(new Date());
        order.setStatus(null); // или задайте конкретный статус
        order.setShippingAddress("123 Main St");
        order.setPaymentMethod(null); // или конкретный метод оплаты
        order.setCustomer(new Customer());

        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        // When + Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St"));
    }

    @Test
    void getOrderById() throws Exception {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(new Date());
        order.setStatus(null);
        order.setShippingAddress("123 Main St");
        order.setPaymentMethod(null);
        order.setCustomer(new Customer());

        when(orderService.getOrderById(1L)).thenReturn(java.util.Optional.of(order));

        // When + Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St"));
    }

    @Test
    void getOrderByIdNotFound() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByCustomerId() throws Exception {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(new Date());
        order.setStatus(null);
        order.setShippingAddress("123 Main St");
        order.setPaymentMethod(null);
        order.setCustomer(new Customer());

        List<Order> orders = new ArrayList<>();
        orders.add(order);

        when(orderService.getOrdersByCustomerId(1L)).thenReturn(orders);

        // When + Then
        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateOrder() throws Exception {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setOrderDate(new Date());
        updatedOrder.setStatus(null);
        updatedOrder.setShippingAddress("456 Oak St");
        updatedOrder.setPaymentMethod(null);
        updatedOrder.setCustomer(new Customer());

        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(updatedOrder);

        // When + Then
        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingAddress").value("456 Oak St"));

        verify(orderService).recalculateOrderTotal(1L);
    }

    @Test
    void updateOrderNotFound() throws Exception {
        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(null);

        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(new Date());
        order.setShippingAddress("789 Pine St");
        order.setPaymentMethod(null);
        order.setCustomer(new Customer());

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }
}