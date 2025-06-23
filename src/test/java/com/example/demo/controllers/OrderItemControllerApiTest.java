package com.example.demo.controllers;

import com.example.demo.controllers.api.OrderItemControllerApi;
import com.example.demo.entities.*;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderItemControllerApiTest {

    private MockMvc mockMvc;
    private OrderItemService orderItemService;
    private OrderService orderService;
    private ProductService productService;
    private OrderItemControllerApi controller;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderItemService = mock(OrderItemService.class);
        orderService = mock(OrderService.class);
        productService = mock(ProductService.class);

        controller = new OrderItemControllerApi(orderItemService, orderService, productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createOrderItem() throws Exception {
        // Given
        Long orderId = 1L;
        Long productId = 2L;
        Integer quantity = 3;

        Order order = new Order();
        order.setId(orderId);

        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("100.00"));

        OrderItemId id = new OrderItemId();
        id.setOrderId(orderId);
        id.setProductId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(quantity);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        when(orderService.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(orderItemService.createOrderItem(any(OrderItem.class))).thenReturn(orderItem);

        // When + Then
        mockMvc.perform(post("/api/order-items/{orderId}/{productId}", orderId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quantity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.order.id").value(1))
                .andExpect(jsonPath("$.product.id").value(2));
    }

    @Test
    void getOrderItemById() throws Exception {
        // Given
        Long orderId = 1L;
        Long productId = 2L;

        Order order = new Order();
        order.setId(orderId);

        Product product = new Product();
        product.setId(productId);

        OrderItemId id = new OrderItemId();
        id.setOrderId(orderId);
        id.setProductId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(5);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(orderItemService.getOrderItemById(id)).thenReturn(Optional.of(orderItem));

        // When + Then
        mockMvc.perform(get("/api/order-items/{orderId}/{productId}", orderId, productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getOrderItemById_OrderOrProductNotFound() throws Exception {
        // Given
        Long orderId = 1L;
        Long productId = 2L;

        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // When + Then
        mockMvc.perform(get("/api/order-items/{orderId}/{productId}", orderId, productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderItemsByOrderId() throws Exception {
        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);

        Product product = new Product();
        product.setId(2L);

        OrderItemId id = new OrderItemId();
        id.setOrderId(orderId);
        id.setProductId(product.getId());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(4);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        List<OrderItem> items = Collections.singletonList(orderItem);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderItemService.getOrderItemsByOrderId(orderId)).thenReturn(items);

        mockMvc.perform(get("/api/order-items/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(4));
    }

    @Test
    void getOrderItemsByProductId() throws Exception {
        Long productId = 2L;

        Order order = new Order();
        order.setId(1L);

        Product product = new Product();
        product.setId(productId);

        OrderItemId id = new OrderItemId();
        id.setOrderId(order.getId());
        id.setProductId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(4);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        List<OrderItem> items = Collections.singletonList(orderItem);

        when(productService.getProductById(productId)).thenReturn(Optional.of(product));
        when(orderItemService.getOrderItemsByProductId(productId)).thenReturn(items);

        mockMvc.perform(get("/api/order-items/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(4));
    }

    @Test
    void deleteOrderItem() throws Exception {
        // Given
        Long orderId = 1L;
        Long productId = 2L;

        Order order = new Order();
        order.setId(orderId);

        Product product = new Product();
        product.setId(productId);

        OrderItemId id = new OrderItemId();
        id.setOrderId(orderId);
        id.setProductId(productId);

        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));

        // When + Then
        mockMvc.perform(delete("/api/order-items/{orderId}/{productId}", orderId, productId))
                .andExpect(status().isNoContent());

        verify(orderItemService).deleteOrderItem(id);
        verify(orderService).recalculateOrderTotal(orderId);
    }

    @Test
    void deleteOrderItem_OrderOrProductNotFound() throws Exception {
        // Given
        Long orderId = 1L;
        Long productId = 2L;

        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // When + Then
        mockMvc.perform(delete("/api/order-items/{orderId}/{productId}", orderId, productId))
                .andExpect(status().isNotFound());
    }
}