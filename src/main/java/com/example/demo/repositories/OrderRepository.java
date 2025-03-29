package com.example.demo.repositories;

import com.example.demo.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.example.demo.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Найти заказы для определенного клиента
    List<Order> findByCustomer_Id(Long customerId);

    // Найти заказы, созданные в определенном диапазоне дат
    List<Order> findByOrderDateBetween(Date startDate, Date endDate);
}
