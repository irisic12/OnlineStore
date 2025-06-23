package com.example.demo.controllers;

import com.example.demo.controllers.api.ReportControllerApi;
import com.example.demo.entities.Order;
import com.example.demo.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ReportControllerApiTest {

    private MockMvc mockMvc;
    private ReportService reportService;
    private ReportControllerApi controller;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        controller = new ReportControllerApi(reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getSalesByProduct() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "Laptop", 100L, new BigDecimal("5000.00")
        });

        when(reportService.getSalesByProductReport(null, null)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/sales-by-product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("Laptop"));
    }

    @Test
    void getSalesByCategory() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "Electronics", new BigDecimal("20000.00")
        });

        when(reportService.getSalesByCategoryReport(null, null)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/sales-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("Electronics"));
    }

    @Test
    void getSalesByCustomer() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "John Doe", new BigDecimal("3000.00")
        });

        when(reportService.getSalesByCustomerReport(null, null)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/sales-by-customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("John Doe"));
    }

    @Test
    void getTopSellingProducts() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "Smartphone", 150L
        });

        when(reportService.getTopNBestSellingProductsReport(null, null, 5)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/top-selling-products")
                        .param("topN", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("Smartphone"));
    }

    @Test
    void getSalesDynamicsByMonth() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "2025-01", new BigDecimal("10000.00")
        });

        when(reportService.getSalesDynamicsByMonthReport(null, null)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/sales-dynamics-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("2025-01"));
    }

    @Test
    void getSalesDynamicsByYear() throws Exception {
        List<Object[]> mockData = Collections.singletonList(new Object[]{
                "2024", new BigDecimal("120000.00")
        });

        when(reportService.getSalesDynamicsByYearReport(null, null)).thenReturn(mockData);

        mockMvc.perform(get("/api/reports/sales-dynamics-year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0][0]").value("2024"));
    }

    @Test
    void getPendingPaymentOrders() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(null); // или конкретный статус
        order.setShippingAddress("123 Main St");

        List<Order> orders = Collections.singletonList(order);
        when(reportService.getPendingPaymentOrdersOlderThanReport(7)).thenReturn(orders);

        mockMvc.perform(get("/api/reports/pending-payment-orders")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].shippingAddress").value("123 Main St"));
    }
}