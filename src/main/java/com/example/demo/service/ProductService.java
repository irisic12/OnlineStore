package com.example.demo.service;

import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> getProductsWithPriceGreaterThan(BigDecimal price) {
        return productRepository.findByPriceGreaterThan(price);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
                    product.setDescription(updatedProduct.getDescription());

                    // Обновляем категорию, если она указана
                    if (updatedProduct.getCategory() != null) {
                        product.setCategory(updatedProduct.getCategory());
                    } else {
                        product.setCategory(null); // Удаляем категорию, если передано null
                    }

                    return productRepository.save(product);
                })
                .orElse(null);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Проверяем, есть ли этот товар в каких-либо заказах
        List<OrderItem> orderItems = orderItemRepository.findById_ProductId(id);
        if (orderItems != null && !orderItems.isEmpty()) {
            throw new IllegalStateException(
                    "Невозможно удалить товар '" + product.getName() +
                            "', так как он присутствует в заказах (" + orderItems.size() + ")."
            );
        }

        productRepository.deleteById(id);
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
