package com.example.demo.service;

import com.example.demo.entities.Cart;
import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;

    @Transactional
    public Cart getCart() {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addToCart(Product product, Integer quantity) {
        Cart cart = getCart();

        // Проверяем, есть ли уже такой товар
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(item.getQuantity() + quantity);
                            cartItemRepository.save(item);
                        },
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProduct(product);
                            newItem.setQuantity(quantity);
                            cartItemRepository.save(newItem);
                        }
                );

        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long productId) {
        Cart cart = getCart();
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(Long productId, Integer quantity) {
        Cart cart = getCart();
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                });
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart() {
        Cart cart = getCart();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);
    }

    @Transactional
    public void deleteCart() {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Удаляем корзину по userId (каскадно удалятся и CartItem)
        cartRepository.deleteByUserId(currentUser.getId());
    }

    public int getCartSize() {
        Cart cart = getCart();
        return cartItemRepository.countByCartId(cart.getId());
    }
}