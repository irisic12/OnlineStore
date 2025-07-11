package com.example.demo.controllers.api;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemControllerApi {
    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final ProductService productService;

    public OrderItemControllerApi(OrderItemService orderItemService,
                                  OrderService orderService,
                                  ProductService productService) {
        this.orderItemService = orderItemService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @PostMapping("/{orderId}/{productId}")
    public ResponseEntity<OrderItem> createOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long productId,
            @RequestBody Integer quantity) {

        Order order = orderService.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        OrderItem orderItem = new OrderItem();
        OrderItemId orderItemId = new OrderItemId();
        orderItemId.setOrderId(order.getId());
        orderItemId.setProductId(product.getId());
        orderItem.setId(orderItemId);
        orderItem.setQuantity(quantity);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        OrderItem savedItem = orderItemService.createOrderItem(orderItem);

        // Явное обновление заказа
        orderService.recalculateOrderTotal(orderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @GetMapping("/{orderId}/{productId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderId,
            @PathVariable Long productId) {

        Optional<Order> order = orderService.getOrderById(orderId);
        Optional<Product> product = productService.getProductById(productId);
        if (!order.isPresent() || !product.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        OrderItemId orderItemId = new OrderItemId();
        orderItemId.setOrderId(order.get().getId());
        orderItemId.setProductId(product.get().getId());

        Optional<OrderItem> orderItem = orderItemService.getOrderItemById(orderItemId);

        return orderItem.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByProductId(@PathVariable Long productId) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByProductId(productId);
        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}/{productId}")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long productId) {

        Optional<Order> order = orderService.getOrderById(orderId);
        Optional<Product> product = productService.getProductById(productId);
        if (!order.isPresent() || !product.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        OrderItemId id = new OrderItemId();
        Order orderEntity = order.get();
        Product productEntity = product.get();

        id.setOrderId(orderEntity.getId());
        id.setProductId(productEntity.getId());

        orderItemService.deleteOrderItem(id);
        orderService.recalculateOrderTotal(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
