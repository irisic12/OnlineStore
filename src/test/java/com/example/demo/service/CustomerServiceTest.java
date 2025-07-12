package com.example.demo.service;

import com.example.demo.entities.Customer;
import com.example.demo.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("1234567890");
        customer.setAddress("123 Main St");
    }

    @Test
    void createCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer created = customerService.createCustomer(customer);

        assertNotNull(created);
        assertEquals("John", created.getFirstName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void getCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> found = customerService.getCustomerById(1L);

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void getCustomerByIdNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Customer> found = customerService.getCustomerById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    void getCustomersByLastName() {
        when(customerRepository.findByLastName("Doe")).thenReturn(Arrays.asList(customer));

        List<Customer> customers = customerService.getCustomersByLastName("Doe");

        assertEquals(1, customers.size());
        assertEquals("John", customers.get(0).getFirstName());
    }

    /*@Test
    void searchCustomersByEmail() {
        OngoingStubbing<Optional<Customer>> optionalOngoingStubbing = when(customerRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Arrays.asList(customer));

        Optional<Customer> customers = customerService.searchCustomerByEmail("john.doe@example.com");

        assertEquals(1, customers.size());
        assertEquals("Doe", customers.get(0).getLastName());
    }*/

    @Test
    void updateCustomer() {
        Customer updatedCustomer = new Customer();
        updatedCustomer.setFirstName("Updated John");
        updatedCustomer.setLastName("Updated Doe");
        updatedCustomer.setEmail("updated.john@example.com");
        updatedCustomer.setPhone("0987654321");
        updatedCustomer.setAddress("456 Updated St");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertNotNull(result);
        assertEquals("Updated John", result.getFirstName());
        assertEquals("Updated Doe", result.getLastName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void updateCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Customer updatedCustomer = new Customer();
        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertNull(result);
    }

    @Test
    void deleteCustomer() {
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void getAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));

        List<Customer> customers = customerService.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("John", customers.get(0).getFirstName());
    }
}
