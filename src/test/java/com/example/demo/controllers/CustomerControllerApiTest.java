package com.example.demo.controllers;

import com.example.demo.controllers.api.CustomerControllerApi;
import com.example.demo.entities.Customer;
import com.example.demo.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerControllerApiTest {

    private MockMvc mockMvc;
    private CustomerService customerService;
    private CustomerControllerApi controller;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerService.class);
        controller = new CustomerControllerApi(customerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createCustomer() throws Exception {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com", "+123456789", "123 Main St");
        when(customerService.createCustomer(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getCustomerById() throws Exception {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com", "+123456789", "123 Main St");
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomersByLastName() throws Exception {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com", "+123456789", "123 Main St");
        List<Customer> customers = Collections.singletonList(customer);
        when(customerService.getCustomersByLastName("Doe")).thenReturn(customers);

        mockMvc.perform(get("/api/customers/lastName/Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    void searchCustomersByEmail() throws Exception {
        Customer customer = new Customer(1L, "John", "Doe", "john@example.com", "+123456789", "123 Main St");
        List<Customer> customers = Collections.singletonList(customer);
        when(customerService.searchCustomersByEmail("john")).thenReturn(customers);

        mockMvc.perform(get("/api/customers/search").param("query", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void updateCustomer() throws Exception {
        Customer updatedCustomer = new Customer(1L, "Jane", "Smith", "jane@example.com", "+987654321", "456 Oak St");
        when(customerService.updateCustomer(anyLong(), any(Customer.class))).thenReturn(updatedCustomer);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void updateCustomerNotFound() throws Exception {
        when(customerService.updateCustomer(anyLong(), any(Customer.class))).thenReturn(null);

        Customer customer = new Customer(1L, "Doesn't", "Matter", "ignore@example.com", "123", "ABC");
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }
}