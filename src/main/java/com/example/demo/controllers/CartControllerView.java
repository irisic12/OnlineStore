package com.example.demo.controllers;

import com.example.demo.entities.Cart;
import com.example.demo.entities.Customer;
import com.example.demo.entities.Order;
import com.example.demo.entities.Product;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.service.CartService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartControllerView {

    private final CartService cartService;
    private final ProductService productService;
    private final CustomerService customerService;

    @GetMapping
    public String viewCart(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            cartService.addToCart(product, quantity);
            redirectAttributes.addFlashAttribute("success", "Товар добавлен в корзину");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Количество обновлено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(productId);
            redirectAttributes.addFlashAttribute("success", "Товар удален из корзины");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        cartService.clearCart();
        redirectAttributes.addFlashAttribute("success", "Корзина очищена");
        return "redirect:/products";
    }

    @GetMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public String checkout(Model model, RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart();
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Корзина пуста");
            return "redirect:/cart";
        }

        // Получаем текущего клиента для предзаполнения формы
        Customer currentCustomer = customerService.getCurrentCustomer()
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        // Создаем новый заказ
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.CREATED);
        order.setCustomer(currentCustomer);

        model.addAttribute("order", order);
        model.addAttribute("currentCustomerId", currentCustomer.getId());
        model.addAttribute("currentCustomerName", currentCustomer.getFullName());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("fromCart", true);  // Флаг, что пришли из корзины

        return "user-order-form";
    }

    @PostMapping("/checkout/confirm")
    public String confirmOrder(RedirectAttributes redirectAttributes) {
        // Здесь будет логика создания заказа из корзины
        cartService.clearCart();
        redirectAttributes.addFlashAttribute("success", "Заказ успешно оформлен");
        return "redirect:/user/orders";
    }
}
