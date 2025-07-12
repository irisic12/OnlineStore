package com.example.demo.controllers;

import com.example.demo.controllers.api.ProductControllerApi;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerApiTest {

    private MockMvc mockMvc;
    private ProductService productService;
    private ProductControllerApi controller;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        controller = new ProductControllerApi(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createProduct() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("High-end gaming laptop");
        product.setPrice(new BigDecimal("1500.00"));
        product.setCategory(category);

        when(productService.createProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500.0));
    }

    @Test
    void getProductById() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setDescription("Latest model");
        product.setPrice(new BigDecimal("800.00"));
        product.setCategory(category);

        when(productService.getProductById(1L)).thenReturn(java.util.Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.price").value(800.0));
    }

    @Test
    void getProductByIdNotFound() throws Exception {
        when(productService.getProductById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductByName() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("Tablet");
        product.setDescription("Lightweight tablet");
        product.setPrice(new BigDecimal("400.00"));
        product.setCategory(category);

        when(productService.getProductByName("Tablet")).thenReturn(product);

        mockMvc.perform(get("/api/products/name/Tablet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tablet"))
                .andExpect(jsonPath("$.price").value(400.0));
    }

    @Test
    void getProductByNameNotFound() throws Exception {
        when(productService.getProductByName("Unknown")).thenReturn(null);

        mockMvc.perform(get("/api/products/name/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchProductsByName() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setDescription("Latest smartphone");
        product.setPrice(new BigDecimal("900.00"));
        product.setCategory(category);

        List<Product> products = Collections.singletonList(product);
        when(productService.searchProductsByName("Pho")).thenReturn(products);

        mockMvc.perform(get("/api/products/search").param("query", "Pho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Phone"));
    }

    @Test
    void getProductsWithPriceGreaterThan() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setName("TV");
        product.setDescription("Large screen TV");
        product.setPrice(new BigDecimal("1200.00"));
        product.setCategory(category);

        List<Product> productList = Collections.singletonList(product);
        when(productService.getProductsWithPriceGreaterThan(new BigDecimal("1000.00")))
                .thenReturn(productList);

        mockMvc.perform(get("/api/products/price/greaterThan/1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1200.0));
    }

    @Test
    void updateProduct() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Laptop");
        updatedProduct.setDescription("New description");
        updatedProduct.setPrice(new BigDecimal("1600.00"));
        updatedProduct.setCategory(category);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void updateProductNotFound() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Doesn't Exist");
        product.setPrice(new BigDecimal("100.00"));

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(null);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void getAllProducts() throws Exception {
        // Создаем категорию
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Gadgets");

        // Создаем продукты
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Headphones");
        product1.setPrice(new BigDecimal("200.00"));
        product1.setCategory(category1);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("30.00"));
        product2.setCategory(category2);

        List<Product> productList = List.of(product1, product2);

        // Заглушка для productService.getProductRepository().findAll()
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.findAll()).thenReturn(productList);
        when(productService.getProductRepository()).thenReturn(productRepository);

        // Выполняем запрос
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Headphones"))
                .andExpect(jsonPath("$[1].name").value("Mouse"));
    }
}