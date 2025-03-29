package com.example.demo.controllers;

import com.example.demo.entities.Order;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Controller
public class ReportControllerView {
    private final ReportService reportService;

    @Autowired
    public ReportControllerView(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/reports/sales-by-product")
    public String getSalesByProduct(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        model.addAttribute("salesData", reportService.getSalesByProductReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report-sales-by-product";
    }

    @GetMapping("/reports/sales-by-category")
    public String getSalesByCategory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        model.addAttribute("salesData", reportService.getSalesByCategoryReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report-sales-by-category";
    }

    @GetMapping("/reports/sales-by-customer")
    public String getSalesByCustomer(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        model.addAttribute("salesData", reportService.getSalesByCustomerReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report-sales-by-customer";
    }

    @GetMapping("/reports/top-selling-products")
    public String getTopSellingProducts(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam int topN,
            Model model) {

        model.addAttribute("salesData", reportService.getTopNBestSellingProductsReport(startDate, endDate, topN));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("topN", topN);
        return "report-top-selling-products";
    }

    @GetMapping("/reports/sales-dynamics-month")
    public String getSalesDynamicsByMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        model.addAttribute("salesData", reportService.getSalesDynamicsByMonthReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report-sales-dynamics-month";
    }

    @GetMapping("/reports/sales-dynamics-year")
    public String getSalesDynamicsByYear(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            Model model) {

        model.addAttribute("salesData", reportService.getSalesDynamicsByYearReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "report-sales-dynamics-year";
    }

    @GetMapping("/reports/pending-payment-orders")
    public String getPendingPaymentOrders(
            @RequestParam int days,
            Model model) {

        model.addAttribute("orders", reportService.getPendingPaymentOrdersOlderThanReport(days));
        model.addAttribute("days", days);
        return "report-pending-payment-orders";
    }
}