package com.example.demo.service;

import com.example.demo.entities.Category;
import com.example.demo.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices");
    }

    @Test
    void createCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category created = categoryService.createCategory(category);

        assertNotNull(created);
        assertEquals("Electronics", created.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void getCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> found = categoryService.getCategoryById(1L);

        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());
    }

    @Test
    void getCategoryByIdNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Category> found = categoryService.getCategoryById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    void getCategoryByName() {
        when(categoryRepository.findByName("Electronics")).thenReturn(category);

        Category found = categoryService.getCategoryByName("Electronics");

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void searchCategoriesByName() {
        when(categoryRepository.findByNameContainingIgnoreCase("elec"))
                .thenReturn(Arrays.asList(category));

        List<Category> categories = categoryService.searchCategoriesByName("elec");

        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void updateCategory() {
        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Electronics");
        updatedCategory.setDescription("Updated description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        Category result = categoryService.updateCategory(1L, updatedCategory);

        assertNotNull(result);
        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void updateCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Category updatedCategory = new Category();
        Category result = categoryService.updateCategory(1L, updatedCategory);

        assertNull(result);
    }

    @Test
    void deleteCategory() {
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void getAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));

        List<Category> categories = categoryService.getAllCategories();

        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }
}
