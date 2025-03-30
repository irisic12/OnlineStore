package com.example.demo.controllers.api;

import com.example.demo.entities.Order;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportControllerApi {
    private final ReportService reportService;

    @Autowired
    public ReportControllerApi(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/sales-by-product")
    public ResponseEntity<List<Object[]>> getSalesByProduct(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Object[]> salesData = reportService.getSalesByProductReport(startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/sales-by-category")
    public ResponseEntity<List<Object[]>> getSalesByCategory(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Object[]> salesData = reportService.getSalesByCategoryReport(startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/sales-by-customer")
    public ResponseEntity<List<Object[]>> getSalesByCustomer(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Object[]> salesData = reportService.getSalesByCustomerReport(startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/top-selling-products")
    public ResponseEntity<List<Object[]>> getTopSellingProducts(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam("topN") int topN) {
        List<Object[]> salesData = reportService.getTopNBestSellingProductsReport(startDate, endDate, topN);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/sales-dynamics-month")
    public ResponseEntity<List<Object[]>> getSalesDynamicsByMonth(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Object[]> salesData = reportService.getSalesDynamicsByMonthReport(startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/sales-dynamics-year")
    public ResponseEntity<List<Object[]>> getSalesDynamicsByYear(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        List<Object[]> salesData = reportService.getSalesDynamicsByYearReport(startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/reports/pending-payment-orders")
    public ResponseEntity<List<Order>> getPendingPaymentOrders(
            @RequestParam("days") int days) {

        List<Order> orders = reportService.getPendingPaymentOrdersOlderThanReport(days);
        return ResponseEntity.ok(orders);
    }
}
