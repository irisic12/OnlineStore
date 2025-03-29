package com.example.demo.repositories;

import com.example.demo.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Найти продукт по имени (точное совпадение)
    Product findByName(String name);

    // Найти продукты, имя которых содержит определенную строку (регистронезависимый поиск)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Найти продукты, цена которых больше определенной суммы
    List<Product> findByPriceGreaterThan(BigDecimal price);

}
