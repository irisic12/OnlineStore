package com.example.demo.controllers;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderControllerView {
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final CustomerService customerService;

    public OrderControllerView(OrderService orderService,
                               OrderItemService orderItemService,
                               ProductService productService,
                               CustomerService customerService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @GetMapping
    public String getAllOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "order-form";
    }

    @PostMapping("/add")
    public String addOrder(
            @ModelAttribute Order order,
            @RequestParam Long customerId,
            Model model) {

        try {
            // Устанавливаем клиента
            Customer customer = customerService.getCustomerById(customerId).orElseThrow();
            order.setCustomer(customer);

            // Устанавливаем текущую дату, если не указана
            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }

            // Проверка обязательных полей
            if (order.getStatus() == null ||
                    order.getShippingAddress() == null || order.getShippingAddress().isEmpty()) {
                throw new IllegalArgumentException("Заполните все обязательные поля");
            }

            orderService.createOrder(order);
            return "redirect:/orders";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("order", order);
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "order-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "order-form";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable Long id,
                              @ModelAttribute Order order,
                              @RequestParam Long customerId,
                              Model model) {
        try {
            Customer customer = customerService.getCustomerById(customerId).orElseThrow();
            order.setCustomer(customer);
            orderService.updateOrder(id, order);
            return "redirect:/orders";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("order", order);
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "order-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "redirect:/orders";
    }
}