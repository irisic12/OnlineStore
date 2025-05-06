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

import java.util.List;
import java.util.Optional;

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
    public String showOrderItems(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("allProducts", productService.getAllProducts());
        return "order-items";
    }

    @PostMapping("/add")
    public String addItemToOrder(@PathVariable Long orderId,
                                 @RequestParam Long productId,
                                 @RequestParam Integer quantity,
                                 Model model) {
        try {
            Order order = orderService.getOrderById(orderId).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            // Создаем OrderItem с правильными связями
            OrderItem orderItem = new OrderItem();

            // Устанавливаем связи через объекты
            orderItem.setOrder(order);
            orderItem.setProduct(product);

            // Создаем и устанавливаем ID
            OrderItemId orderItemId = new OrderItemId();
            orderItemId.setOrderId(order.getId());
            orderItemId.setProductId(product.getId());
            orderItem.setId(orderItemId);

            orderItem.setQuantity(quantity);

            orderItemService.createOrderItem(orderItem);
            orderService.recalculateOrderTotal(orderId);
            return "redirect:/orders/" + orderId + "/items";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при добавлении товара: " + e.getMessage());
            return "redirect:/orders/" + orderId + "/items?error=" + e.getMessage();
        }
    }

    @PostMapping("/update")
    public String updateOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer quantity,
                                  Model model) {
        try {
            Order order = orderService.getOrderById(orderId).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            OrderItemId id = new OrderItemId();
            id.setOrderId(order.getId());
            id.setProductId(product.getId());

            Optional<OrderItem> orderItemOpt = orderItemService.getOrderItemById(id);
            if (orderItemOpt.isPresent()) {
                OrderItem orderItem = orderItemOpt.get();
                orderItem.setQuantity(quantity);
                orderItemService.createOrderItem(orderItem);
                orderService.recalculateOrderTotal(orderId);
            } else {
                model.addAttribute("error", "Товар в заказе не найден");
            }

            return "redirect:/orders/" + orderId + "/items";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обновлении товара: " + e.getMessage());
            return "redirect:/orders/" + orderId + "/items?error=" + e.getMessage();
        }
    }

    @PostMapping("/delete")
    public String deleteOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId,
                                  Model model) {
        try {
            Order order = orderService.getOrderById(orderId).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            OrderItemId id = new OrderItemId();
            id.setOrderId(order.getId());
            id.setProductId(product.getId());

            orderItemService.deleteOrderItem(id);
            orderService.recalculateOrderTotal(orderId);
            return "redirect:/orders/" + orderId + "/items";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при удалении товара: " + e.getMessage());
            return "redirect:/orders/" + orderId + "/items?error=" + e.getMessage();
        }
    }
}