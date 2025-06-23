package com.example.demo.service;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.helpClass.OrderItemId;
import com.example.demo.repositories.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {
    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private OrderItemService orderItemService;

    private OrderItem orderItem;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setPrice(BigDecimal.valueOf(999.99));

        order = new Order();
        order.setId(1L);
        order.setTotalAmount(BigDecimal.ZERO);

        OrderItemId orderItemId = new OrderItemId(1L, 1L);
        orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
    }

    @Test
    void createOrderItem_shouldSaveOrderItemAndUpdateOrder() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        Order order = new Order();
        order.setId(1L);
        orderItem.setOrder(order);

        OrderItem savedOrderItem = new OrderItem();
        savedOrderItem.setOrder(order);

        when(orderItemRepository.save(orderItem)).thenReturn(savedOrderItem);
        when(orderService.findByIdWithItems(order.getId())).thenReturn(Optional.of(order));

        // Act
        OrderItem result = orderItemService.createOrderItem(orderItem);

        // Assert
        assertNotNull(result);
        verify(orderItemRepository).save(orderItem);
        verify(orderService).findByIdWithItems(order.getId());
        verify(orderService).updateOrder(eq(order.getId()), argThat(updateRequest ->
                updateRequest.getId().equals(order.getId()) &&
                        updateRequest.getTotalAmount() == null // Мы явно обнуляем сумму для пересчета
        ));
    }


    @Test
    @Transactional
    void createOrderItemWithoutOrder() {
        orderItem.setOrder(null); // Тестируем случай без привязки к заказу
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItem created = orderItemService.createOrderItem(orderItem);

        assertNotNull(created);
        assertNull(created.getOrder());
        verify(orderItemRepository, times(1)).save(orderItem);
        verify(orderService, never()).recalculateOrderTotal(anyLong());
    }

    @Test
    void getOrderItemById() {
        OrderItemId id = new OrderItemId(1L, 1L);
        when(orderItemRepository.findById(id)).thenReturn(Optional.of(orderItem));

        Optional<OrderItem> found = orderItemService.getOrderItemById(id);

        assertTrue(found.isPresent());
        assertEquals(2, found.get().getQuantity());
    }

    @Test
    void getOrderItemsByOrderId() {
        when(orderItemRepository.findById_OrderId(1L)).thenReturn(Arrays.asList(orderItem));

        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(1L);

        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId().getOrderId());
    }

    @Test
    void getOrderItemsByProductId() {
        when(orderItemRepository.findById_ProductId(1L)).thenReturn(Arrays.asList(orderItem));

        List<OrderItem> items = orderItemService.getOrderItemsByProductId(1L);

        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId().getProductId());
    }

    @Test
    void deleteOrderItem() {
        OrderItemId id = new OrderItemId(1L, 1L);
        doNothing().when(orderItemRepository).deleteById(id);

        orderItemService.deleteOrderItem(id);

        verify(orderItemRepository, times(1)).deleteById(id);
        verify(orderService, times(1)).recalculateOrderTotal(1L);
    }
}
