package com.example.demo.controllers;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = customerService.getCustomerById(customerId).orElseThrow();
            order.setCustomer(customer);
            order.setTotalAmount(BigDecimal.ZERO);

            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }

            Order savedOrder = orderService.createOrder(order);

            // После создания сразу переходим на страницу добавления товаров
            redirectAttributes.addFlashAttribute("info", "Заказ создан. Теперь добавьте товары.");
            return "redirect:/orders/" + savedOrder.getId() + "/items";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/add";
        }
    }

    @PostMapping("/add-and-go-to-items")
    public String addOrderAndGoToItems(
            @ModelAttribute Order order,
            @RequestParam Long customerId,
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = customerService.getCustomerById(customerId).orElseThrow();
            order.setCustomer(customer);
            order.setTotalAmount(BigDecimal.ZERO);

            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }

            Order savedOrder = orderService.createOrder(order);

            // Передаем параметр, что это новый заказ
            return "redirect:/orders/" + savedOrder.getId() + "/items?new=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id).orElseThrow();

        // Получаем количество товаров в заказе
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(id);
        int itemsCount = orderItems != null ? orderItems.size() : 0;

        model.addAttribute("order", order);
        model.addAttribute("orderItemsCount", itemsCount);
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
            // Получаем существующий заказ из БД
            Order existingOrder = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем наличие товаров
            List<OrderItem> existingItems = orderItemService.getOrderItemsByOrderId(id);
            if (existingItems == null || existingItems.isEmpty()) {
                model.addAttribute("error", "НЕЛЬЗЯ СОХРАНИТЬ ЗАКАЗ БЕЗ ТОВАРОВ. Сначала добавьте товары.");
                model.addAttribute("order", existingOrder);
                model.addAttribute("customers", customerService.getAllCustomers());
                model.addAttribute("statuses", OrderStatus.values());
                model.addAttribute("paymentMethods", PaymentMethod.values());

                // Добавляем количество товаров для отображения
                model.addAttribute("orderItemsCount", 0);
                return "order-form";
            }

            // Обновляем только поля заказа из формы
            existingOrder.setOrderDate(order.getOrderDate());
            existingOrder.setStatus(order.getStatus());
            existingOrder.setPaymentMethod(order.getPaymentMethod());
            existingOrder.setShippingAddress(order.getShippingAddress());

            // Обновляем клиента
            Customer customer = new Customer();
            customer.setId(customerId);
            existingOrder.setCustomer(customer);

            // Сохраняем заказ (НЕ создаем новый объект)
            orderService.updateOrder(id, existingOrder);

            // Пересчитываем сумму
            orderService.recalculateOrderTotal(id);

            return "redirect:/orders";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка при сохранении: " + e.getMessage());

            // Загружаем заказ заново для отображения в форме
            Order orderForForm = orderService.getOrderById(id).orElse(order);
            model.addAttribute("order", orderForForm);
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());

            // Добавляем количество товаров
            try {
                List<OrderItem> items = orderItemService.getOrderItemsByOrderId(id);
                model.addAttribute("orderItemsCount", items != null ? items.size() : 0);
            } catch (Exception ex) {
                model.addAttribute("orderItemsCount", 0);
            }
            return "order-form";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Заказ удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления: " + e.getMessage());
        }
        return "redirect:/orders";
    }
}