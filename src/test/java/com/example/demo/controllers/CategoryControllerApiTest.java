package com.example.demo.controllers;

import com.example.demo.controllers.api.CategoryControllerApi;
import com.example.demo.entities.Category;
import com.example.demo.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CategoryControllerApiTest {

    private MockMvc mockMvc;
    private CategoryService categoryService;
    private CategoryControllerApi controller;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryService.class);
        controller = new CategoryControllerApi(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createCategory() throws Exception {
        Category category = new Category(1L, "Electronics", "Electronic devices");
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryById() throws Exception {
        Category category = new Category(1L, "Electronics", "Electronic devices");
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryByIdNotFound() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoryByName() throws Exception {
        Category category = new Category(1L, "Electronics", "Electronic devices");
        when(categoryService.getCategoryByName("Electronics")).thenReturn(category);

        mockMvc.perform(get("/api/categories/name/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryByNameNotFound() throws Exception {
        when(categoryService.getCategoryByName("Unknown")).thenReturn(null);

        mockMvc.perform(get("/api/categories/name/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchCategoriesByName() throws Exception {
        Category category = new Category(1L, "Electronics", "Electronic devices");
        when(categoryService.searchCategoriesByName("elec"))
                .thenReturn(Collections.singletonList(category));

        mockMvc.perform(get("/api/categories/search").param("name", "elec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void updateCategory() throws Exception {
        Category updatedCategory = new Category(1L, "Updated Electronics", "Updated description");
        when(categoryService.updateCategory(anyLong(), any(Category.class))).thenReturn(updatedCategory);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    void updateCategoryNotFound() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(Category.class))).thenReturn(null);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Category())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }
}