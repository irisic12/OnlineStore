package com.example.demo.controllers;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders/{orderId}/items")
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
    public String showOrderItems(@PathVariable Long orderId,
                                 @RequestParam(required = false) String from,
                                 Model model) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);

            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("allProducts", productService.getAllProducts());
            model.addAttribute("hasItems", !orderItems.isEmpty());

            if ("list".equals(from)) {
                model.addAttribute("backUrl", "/orders");
                model.addAttribute("backText", "Назад к списку заказов");
            } else {
                model.addAttribute("backUrl", "/orders/edit/" + orderId);
                model.addAttribute("backText", "Вернуться к заказу");
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "order-items";
    }

    @PostMapping("/add")
    public String addItem(@PathVariable Long orderId,
                          @RequestParam Long productId,
                          @RequestParam Integer quantity,
                          @RequestParam(required = false) String from,
                          RedirectAttributes redirectAttributes) {

        try {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());
            item.setId(new OrderItemId(orderId, productId));

            orderService.addItemToOrder(orderId, item);

            redirectAttributes.addFlashAttribute("successMessage", "Товар успешно добавлен");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
        return "redirect:/orders/" + orderId + "/items" + fromParam;
    }

    @PostMapping("/update")
    public String updateOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer quantity,
                                  @RequestParam(required = false) String from,
                                  RedirectAttributes redirectAttributes) {
        try {
            OrderItemId id = new OrderItemId(orderId, productId);
            OrderItem orderItem = orderItemService.getOrderItemById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден в заказе"));

            orderItem.setQuantity(quantity);
            orderItemService.createOrderItem(orderItem);
            orderService.recalculateOrderTotal(orderId);

            redirectAttributes.addFlashAttribute("successMessage", "Количество обновлено");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/items" + (from != null ? "?from=" + from : "");
    }

    @PostMapping("/delete")
    public String deleteItem(@PathVariable Long orderId,
                             @RequestParam Long productId,
                             @RequestParam(required = false) String from,
                             RedirectAttributes redirectAttributes) {
        try {
            OrderItemId itemId = new OrderItemId(orderId, productId);
            orderService.removeItemFromOrder(orderId, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Товар удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/items" + (from != null ? "?from=" + from : "");
    }

    @GetMapping("/cancel")
    public String cancelOrderCreation(@RequestParam Long orderId,
                                      @RequestParam(required = false) String from,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Проверяем, это новый заказ или существующий
            // Можно передавать параметр isNew=true при создании
            if ("new".equals(from)) {
                // Удаляем заказ вместе со всеми товарами
                orderService.deleteOrderWithItems(orderId);
                redirectAttributes.addFlashAttribute("info", "Создание заказа отменено");
            }
            // Если это редактирование - просто возвращаемся без удаления
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отмене: " + e.getMessage());
            return "redirect:/orders";
        }
    }
}