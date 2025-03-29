package com.example.demo.controllers;

import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.enums.OrderStatus;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderControllerView {
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    public OrderControllerView(OrderService orderService,
                               OrderItemService orderItemService,
                               ProductService productService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    @GetMapping("/{orderId}/items")
    public String showOrderItems(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("allProducts", productService.getAllProducts());
        return "order-items";
    }

    @PostMapping("/{orderId}/items/add")
    public String addItemToOrder(@PathVariable Long orderId,
                                 @RequestParam Long productId,
                                 @RequestParam Integer quantity) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        Product product = productService.getProductById(productId).orElseThrow();

        OrderItem orderItem = new OrderItem();
        OrderItemId orderItemId = new OrderItemId();
        orderItemId.setOrder(order);
        orderItemId.setProduct(product);
        orderItem.setId(orderItemId);
        orderItem.setQuantity(quantity);

        orderItemService.createOrderItem(orderItem);
        return "redirect:/orders/" + orderId + "/items";
    }

    @PostMapping("/{orderId}/items/update")
    public String updateOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer quantity) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        Product product = productService.getProductById(productId).orElseThrow();

        OrderItemId id = new OrderItemId();
        id.setOrder(order);
        id.setProduct(product);

        OrderItem orderItem = orderItemService.getOrderItemById(id).orElseThrow();
        orderItem.setQuantity(quantity);
        orderItemService.createOrderItem(orderItem);

        return "redirect:/orders/" + orderId + "/items";
    }

    @PostMapping("/{orderId}/items/delete")
    public String deleteOrderItem(@PathVariable Long orderId,
                                  @RequestParam Long productId) {
        Order order = orderService.getOrderById(orderId).orElseThrow();
        Product product = productService.getProductById(productId).orElseThrow();

        OrderItemId id = new OrderItemId();
        id.setOrder(order);
        id.setProduct(product);

        orderItemService.deleteOrderItem(id);
        return "redirect:/orders/" + orderId + "/items";
    }
}