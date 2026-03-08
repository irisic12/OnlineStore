package com.example.demo.controllers;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.entities.Cart;
import com.example.demo.entities.CartItem;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.service.CartService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.helpClass.OrderItemId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final ReviewService reviewService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String getUserProfile(Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            CustomerRequestDTO customerRequest = CustomerRequestDTO.builder()
                    .id(currentCustomer.getId())
                    .firstName(currentCustomer.getFirstName())
                    .lastName(currentCustomer.getLastName())
                    .email(currentCustomer.getUser() != null ? currentCustomer.getUser().getEmail() : "")
                    .phone(currentCustomer.getPhone())
                    .address(currentCustomer.getAddress())
                    .build();

            model.addAttribute("customerRequest", customerRequest);
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("isNewUser", false);

            return "customer-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки профиля: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('USER')")
    public String updateUserProfile(
            @Valid @ModelAttribute("customerRequest") CustomerRequestDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("isNewUser", false);
            return "customer-form";
        }

        try {
            Customer customer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .build();

            customerService.updateCustomer(
                    request.getId(),
                    customer,
                    request.getEmail(),
                    false
            );

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customerRequest", request);
            model.addAttribute("isAdmin", false);
            model.addAttribute("actionUrl", "/user/profile/update");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("isNewUser", false);
            return "customer-form";
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public String getUserOrders(Model model) {
        // Получаем текущего клиента
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        // Получаем заказы только этого клиента
        List<Order> userOrders = orderService.getOrdersByCustomerId(currentCustomer.getId());

        model.addAttribute("orders", userOrders);
        return "user-orders";
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('USER')")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем заказ
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            model.addAttribute("order", order);
            return "user-orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @GetMapping("/orders/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public String deleteUserOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем заказ
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            // Проверяем статус заказа для удаления
            if (order.getStatus() == OrderStatus.CREATED ||
                    order.getStatus() == OrderStatus.PAID ||
                    order.getStatus() == OrderStatus.IN_PROCESSING) {

                orderService.deleteOrder(id);
                redirectAttributes.addFlashAttribute("success", "Заказ успешно удален");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Нельзя удалить заказ со статусом '" +
                                order.getStatus() + "'. Доступны для удаления только заказы со статусами: Создан, Оплачен, В обработке");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/user/orders";
    }

    @GetMapping("/orders/add")
    @PreAuthorize("hasRole('USER')")
    public String showUserAddOrderForm(Model model) {
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.CREATED);

        model.addAttribute("order", order);
        model.addAttribute("currentCustomerId", currentCustomer.getId());
        model.addAttribute("currentCustomerName", currentCustomer.getFullName());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "user-order-form";
    }

    @PostMapping("/orders/add")
    @PreAuthorize("hasRole('USER')")
    public String addUserOrder(
            @ModelAttribute Order order,
            @RequestParam Long customerId,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) Boolean fromCart,
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = customerService.getCustomerById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            order.setCustomer(customer);
            order.setOrderDate(new Date());
            order.setStatus(OrderStatus.CREATED);
            order.setPaymentMethod(paymentMethod);
            order.setShippingAddress(shippingAddress);
            order.setTotalAmount(BigDecimal.ZERO);

            Order savedOrder = orderService.createOrder(order);
            //log.info("Заказ создан: ID={}", savedOrder.getId());

            // Если пришли из корзины
            if (fromCart != null && fromCart) {

                // Получаем корзину из БД через сервис
                Cart cart = cartService.getCart();
                //log.info("Корзина: items count = {}", cart.getItems().size());

                if (cart != null && !cart.getItems().isEmpty()) {

                    //log.info("Добавление товаров из корзины в заказ #{}", savedOrder.getId());

                    // Добавляем все товары из корзины в заказ
                    for (CartItem cartItem : cart.getItems()) {
                        Product product = cartItem.getProduct();
                        Integer quantity = cartItem.getQuantity();

                        //log.info("Обработка товара: {} x {}", product.getName(), quantity);

                        OrderItem orderItem = new OrderItem();
                        orderItem.setProduct(product);
                        orderItem.setQuantity(quantity);
                        orderItem.setPrice(product.getPrice());
                        orderItem.setId(new OrderItemId(savedOrder.getId(), product.getId()));

                        // Сохраняем в БД
                        orderService.addItemToOrder(savedOrder.getId(), orderItem);
                        //log.info("Товар добавлен в заказ: {} x {}", product.getName(), quantity);
                    }

                    // Очищаем корзину в БД
                    cartService.deleteCart();
                    //log.info("Корзина очищена");

                    redirectAttributes.addFlashAttribute("success",
                            "Заказ создан и товары из корзины добавлены.");

                    return "redirect:/user/orders/" + savedOrder.getId() + "/items?new=true";
                } else {
                    //log.warn("Корзина пуста");
                }
            }

            redirectAttributes.addFlashAttribute("success", "Заказ создан. Теперь добавьте товары.");
            return "redirect:/user/orders/" + savedOrder.getId() + "/items?new=true";

        } catch (Exception e) {
            //log.error("Ошибка при создании заказа: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders/add";
        }
    }

    @GetMapping("/orders/{id}/items")
    @PreAuthorize("hasRole('USER')")
    public String showUserOrderItems(@PathVariable Long id,
                                     @RequestParam(required = false) String from,
                                     @RequestParam(required = false) String source,  // ← новый параметр
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(id);

            // Получаем все отзывы пользователя на товары в этом заказе
            Map<Long, ReviewResponseDTO> reviewMap = new HashMap<>();
            for (OrderItem item : orderItems) {
                reviewService.getUserReviewForProductInOrder(id, item.getProduct().getId())
                        .ifPresent(review -> reviewMap.put(item.getProduct().getId(), review));
            }

            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("allProducts", productService.getAllProducts());
            model.addAttribute("reviewMap", reviewMap);

            // ЛОГИКА:
            // Редактирование доступно ТОЛЬКО если:
            // 1. Заказ имеет статус CREATED
            // 2. Мы НЕ пришли из списка заказов (from не равен "list")
            // 3. Мы НЕ пришли из формы заказа (source не равен "form")
            boolean canEdit = order.getStatus() == OrderStatus.CREATED
                    && !"list".equals(from)
                    && !"form".equals(source);

            model.addAttribute("canEdit", canEdit);

            // Определяем текст кнопки возврата
            if ("list".equals(from)) {
                model.addAttribute("backUrl", "/user/orders");
                model.addAttribute("backText", "Назад к списку заказов");
            } else {
                model.addAttribute("backUrl", "/user/orders/edit/" + id);
                model.addAttribute("backText", "Вернуться к заказу");
            }

            return "user-order-items";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/orders/update/{id}")
    @PreAuthorize("hasRole('USER')")
    public String updateUserOrder(@PathVariable Long id,
                                  @ModelAttribute Order order,
                                  @RequestParam Long customerId,
                                  @RequestParam PaymentMethod paymentMethod,
                                  @RequestParam String shippingAddress,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем существующий заказ
            Order existingOrder = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!existingOrder.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            // Обновляем только разрешенные поля
            existingOrder.setPaymentMethod(paymentMethod);
            existingOrder.setShippingAddress(shippingAddress);
            // Дата заказа и статус не меняются

            orderService.updateOrder(id, existingOrder);

            redirectAttributes.addFlashAttribute("success", "Заказ успешно обновлен");
            return "redirect:/user/orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
            return "redirect:/user/orders/edit/" + id;
        }
    }

    @GetMapping("/orders/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String showUserEditOrderForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            // Запрещаем редактирование - только просмотр
            model.addAttribute("order", order);
            model.addAttribute("currentCustomerId", currentCustomer.getId());
            model.addAttribute("currentCustomerName", currentCustomer.getFullName());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("readonly", true);  // Флаг только для чтения

            return "user-order-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/orders/{orderId}/items/add")
    @PreAuthorize("hasRole('USER')")
    public String addUserOrderItem(@PathVariable Long orderId,
                                   @RequestParam Long productId,
                                   @RequestParam Integer quantity,
                                   @RequestParam(required = false) String from,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем заказ
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            // Получаем товар
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            // Проверяем, есть ли уже такой товар в заказе
            List<OrderItem> existingItems = orderItemService.getOrderItemsByOrderId(orderId);
            boolean productExists = existingItems.stream()
                    .anyMatch(item -> item.getProduct().getId().equals(productId));

            if (productExists) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Товар \"" + product.getName() + "\" уже есть в заказе");
                String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
                return "redirect:/user/orders/" + orderId + "/items" + fromParam;
            }

            // Создаем элемент заказа
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());
            item.setId(new OrderItemId(orderId, productId));

            // Добавляем в заказ
            orderService.addItemToOrder(orderId, item);

            redirectAttributes.addFlashAttribute("successMessage", "Товар успешно добавлен");

            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;
        }
    }

    @PostMapping("/orders/{orderId}/items/update")
    @PreAuthorize("hasRole('USER')")
    public String updateUserOrderItem(@PathVariable Long orderId,
                                      @RequestParam Long productId,
                                      @RequestParam Integer quantity,
                                      @RequestParam(required = false) String from,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем заказ
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

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

            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;
        }
    }

    @PostMapping("/orders/{orderId}/items/delete")
    @PreAuthorize("hasRole('USER')")
    public String deleteUserOrderItem(@PathVariable Long orderId,
                                      @RequestParam Long productId,
                                      @RequestParam(required = false) String from,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Получаем текущего клиента
            Customer currentCustomer = customerService.getCurrentCustomer()
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Получаем заказ
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "У вас нет доступа к этому заказу");
                return "redirect:/user/orders";
            }

            OrderItemId itemId = new OrderItemId(orderId, productId);
            orderService.removeItemFromOrder(orderId, itemId);

            redirectAttributes.addFlashAttribute("successMessage", "Товар удален");

            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            String fromParam = (from != null && !from.isEmpty()) ? "?from=" + from : "";
            return "redirect:/user/orders/" + orderId + "/items" + fromParam;
        }
    }
}