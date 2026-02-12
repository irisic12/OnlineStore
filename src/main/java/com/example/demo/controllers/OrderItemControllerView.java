package com.example.demo.controllers;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders/{orderId}/items")
@PreAuthorize("hasRole('ADMIN')")
public class OrderItemControllerView {
    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final ProductService productService;

    public OrderItemControllerView(OrderItemService orderItemService,
                                   OrderService orderService,
                                   ProductService productService) {
        this.orderItemService = orderItemService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    public String showOrderItems(@PathVariable Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);

            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("allProducts", productService.getAllProducts());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "order-items";
    }

    @PostMapping("/add")
    public String addItem(@PathVariable Long orderId,
                          @RequestParam Long productId,
                          @RequestParam Integer quantity,
                          RedirectAttributes redirectAttributes) {

        try {
            // Получаем товар
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            // Создаем элемент заказа с фиксацией цены
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice()); // ← ФИКСИРУЕМ ЦЕНУ НА МОМЕНТ ДОБАВЛЕНИЯ
            item.setId(new OrderItemId(orderId, productId));

            // Добавляем в заказ
            orderService.addItemToOrder(orderId, item);

            redirectAttributes.addFlashAttribute("successMessage", "Товар успешно добавлен");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/items";
    }

    @PostMapping("/update")
    public String updateOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer quantity,
                                  RedirectAttributes redirectAttributes) {
        try {
            OrderItemId id = new OrderItemId(orderId, productId);

            // Получаем существующий элемент
            OrderItem orderItem = orderItemService.getOrderItemById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден в заказе"));

            // Обновляем количество
            orderItem.setQuantity(quantity);
            orderItemService.createOrderItem(orderItem);

            // Пересчитываем сумму заказа
            orderService.recalculateOrderTotal(orderId);

            redirectAttributes.addFlashAttribute("successMessage", "Количество обновлено");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/items";
    }

    @PostMapping("/delete")
    public String deleteItem(@PathVariable Long orderId,
                             @RequestParam Long productId,
                             RedirectAttributes redirectAttributes) {
        try {
            OrderItemId itemId = new OrderItemId(orderId, productId);
            orderService.removeItemFromOrder(orderId, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Товар удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/items";
    }
}