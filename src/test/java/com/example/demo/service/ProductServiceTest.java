package com.example.demo.service;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setDescription("Latest model");
        product.setCategory(category);
    }

    @Test
    void createProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product created = productService.createProduct(product);

        assertNotNull(created);
        assertEquals("Smartphone", created.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void getProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> found = productService.getProductById(1L);

        assertTrue(found.isPresent());
        assertEquals("Smartphone", found.get().getName());
    }

    @Test
    void getProductByIdNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> found = productService.getProductById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    void getProductByName() {
        when(productRepository.findByName("Smartphone")).thenReturn(product);

        Product found = productService.getProductByName("Smartphone");

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void searchProductsByName() {
        when(productRepository.findByNameContainingIgnoreCase("smart"))
                .thenReturn(Arrays.asList(product));

        List<Product> products = productService.searchProductsByName("smart");

        assertEquals(1, products.size());
        assertEquals("Smartphone", products.get(0).getName());
    }

    @Test
    void getProductsWithPriceGreaterThan() {
        BigDecimal price = BigDecimal.valueOf(500);
        when(productRepository.findByPriceGreaterThan(price))
                .thenReturn(Arrays.asList(product));

        List<Product> products = productService.getProductsWithPriceGreaterThan(price);

        assertEquals(1, products.size());
        assertTrue(products.get(0).getPrice().compareTo(price) > 0);
    }

    @Test
    void updateProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Smartphone");
        updatedProduct.setPrice(BigDecimal.valueOf(1099.99));
        updatedProduct.setDescription("Updated description");
        updatedProduct.setCategory(category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertNotNull(result);
        assertEquals("Updated Smartphone", result.getName());
        assertEquals(BigDecimal.valueOf(1099.99), result.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Product updatedProduct = new Product();
        Product result = productService.updateProduct(1L, updatedProduct);

        assertNull(result);
    }

    @Test
    void deleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void getAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Smartphone", products.get(0).getName());
    }
}
