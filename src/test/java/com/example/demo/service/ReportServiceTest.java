package com.example.demo.service;

import com.example.demo.entities.Order;
import com.example.demo.repositories.CustomOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @Mock
    private CustomOrderRepository orderRepository;

    @InjectMocks
    private ReportService reportService;

    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        startDate = new Date(System.currentTimeMillis() - 86400000); // yesterday
        endDate = new Date();
    }

    @Test
    void getSalesByProductReport() {
        // Подготовка тестовых данных
        Object[] reportData = new Object[]{"Product1", BigDecimal.valueOf(1000)};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        // Настройка mock
        when(orderRepository.findSalesByProductForPeriod(startDate, endDate))
                .thenReturn(expectedResult);

        // Вызов тестируемого метода
        List<Object[]> result = reportService.getSalesByProductReport(startDate, endDate);

        // Проверки
        assertEquals(1, result.size());
        assertEquals("Product1", result.get(0)[0]);
        assertEquals(BigDecimal.valueOf(1000), result.get(0)[1]);
    }

    @Test
    void getSalesByCategoryReport() {
        Object[] reportData = new Object[]{"Category1", BigDecimal.valueOf(2000)};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        when(orderRepository.findSalesByCategoryForPeriod(startDate, endDate))
                .thenReturn(expectedResult);

        List<Object[]> result = reportService.getSalesByCategoryReport(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("Category1", result.get(0)[0]);
        assertEquals(BigDecimal.valueOf(2000), result.get(0)[1]);
    }

    @Test
    void getSalesByCustomerReport() {
        Object[] reportData = new Object[]{"John", "Doe", BigDecimal.valueOf(3000)};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        when(orderRepository.findSalesByCustomerForPeriod(startDate, endDate))
                .thenReturn(expectedResult);

        List<Object[]> result = reportService.getSalesByCustomerReport(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("John", result.get(0)[0]);
        assertEquals("Doe", result.get(0)[1]);
        assertEquals(BigDecimal.valueOf(3000), result.get(0)[2]);
    }

    @Test
    void getTopNBestSellingProductsReport() {
        Object[] reportData = new Object[]{"Product1", 10};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        when(orderRepository.findTopNBestSellingProductsForPeriod(startDate, endDate, 5))
                .thenReturn(expectedResult);

        List<Object[]> result = reportService.getTopNBestSellingProductsReport(startDate, endDate, 5);

        assertEquals(1, result.size());
        assertEquals("Product1", result.get(0)[0]);
        assertEquals(10, result.get(0)[1]);
    }

    @Test
    void getSalesDynamicsByMonthReport() {
        Object[] reportData = new Object[]{2023, 5, BigDecimal.valueOf(4000)};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        when(orderRepository.getSalesDynamicsByMonth(startDate, endDate))
                .thenReturn(expectedResult);

        List<Object[]> result = reportService.getSalesDynamicsByMonthReport(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(2023, result.get(0)[0]);
        assertEquals(5, result.get(0)[1]);
        assertEquals(BigDecimal.valueOf(4000), result.get(0)[2]);
    }

    @Test
    void getSalesDynamicsByYearReport() {
        Object[] reportData = new Object[]{2023, BigDecimal.valueOf(5000)};
        List<Object[]> expectedResult = Collections.singletonList(reportData);

        when(orderRepository.getSalesDynamicsByYear(startDate, endDate))
                .thenReturn(expectedResult);

        List<Object[]> result = reportService.getSalesDynamicsByYearReport(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(2023, result.get(0)[0]);
        assertEquals(BigDecimal.valueOf(5000), result.get(0)[1]);
    }

    @Test
    void getPendingPaymentOrdersOlderThanReport() {
        Order order = new Order();
        order.setId(1L);
        List<Order> expectedResult = Collections.singletonList(order);

        // Используем any() для даты, так как точное значение не важно для теста
        when(orderRepository.findPendingPaymentOrdersOlderThan(any(Date.class)))
                .thenReturn(expectedResult);

        // Вызываем тестируемый метод
        List<Order> result = reportService.getPendingPaymentOrdersOlderThanReport(7);

        // Проверяем результаты
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        // Можно дополнительно проверить, что метод был вызван
        verify(orderRepository).findPendingPaymentOrdersOlderThan(any(Date.class));
    }
}
