package com.example.demo.service;

import com.example.demo.entities.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.CustomOrderRepository;
import com.example.demo.repositories.CustomerRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final CustomOrderRepository orderRepository;

    @Autowired
    public ReportService(CustomOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Object[]> getSalesByProductReport(Date startDate, Date endDate) {
        return orderRepository.findSalesByProductForPeriod(startDate, endDate);
    }

    public List<Object[]> getSalesByCategoryReport(Date startDate, Date endDate) {
        return orderRepository.findSalesByCategoryForPeriod(startDate, endDate);
    }

    public List<Object[]> getSalesByCustomerReport(Date startDate, Date endDate) {
        return orderRepository.findSalesByCustomerForPeriod(startDate, endDate);
    }

    public List<Object[]> getTopNBestSellingProductsReport(Date startDate, Date endDate, int topN) {
        return orderRepository.findTopNBestSellingProductsForPeriod(startDate, endDate, topN);
    }

    public List<Object[]> getSalesDynamicsByMonthReport(Date startDate, Date endDate) {
        return orderRepository.getSalesDynamicsByMonth(startDate, endDate);
    }

    public List<Object[]> getSalesDynamicsByYearReport(Date startDate, Date endDate) {
        return orderRepository.getSalesDynamicsByYear(startDate, endDate);
    }

    public List<Order> getPendingPaymentOrdersOlderThanReport(int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(java.util.Calendar.DATE, -days);
        Date dateThreshold = cal.getTime();

        return orderRepository.findPendingPaymentOrdersOlderThan(dateThreshold);
    }
}
