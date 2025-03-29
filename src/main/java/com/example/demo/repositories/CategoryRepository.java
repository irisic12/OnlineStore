package com.example.demo.repositories;

import com.example.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Найти категорию по имени (точное совпадение)
    Category findByName(String name);

    // Найти категории, имя которых содержит определенную строку (регистронезависимый поиск)
    List<Category> findByNameContainingIgnoreCase(String name);
}
