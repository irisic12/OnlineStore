package com.example.demo.repositories;

import com.example.demo.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.enums.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface CustomOrderRepository extends JpaRepository<Order, Long>, ReportRepository{
    @Override
    @Query("SELECT oi.product.name, SUM(oi.quantity * oi.product.price) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.product.name")
    List<Object[]> findSalesByProductForPeriod(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Override
    @Query("SELECT oi.product.category.name, SUM(oi.quantity * oi.product.price) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.product.category.name")
    List<Object[]> findSalesByCategoryForPeriod(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Override
    @Query("SELECT o.customer.firstName, o.customer.lastName, SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY o.customer.firstName, o.customer.lastName")
    List<Object[]> findSalesByCustomerForPeriod(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Override
    @Query(value = "SELECT p.name, SUM(oi.quantity) AS total_quantity " +
            "FROM order_item oi " +
            "JOIN product p ON oi.product_id = p.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.order_date BETWEEN :startDate AND :endDate " +
            "GROUP BY p.name " +
            "ORDER BY total_quantity DESC " +
            "LIMIT :topN", nativeQuery = true)
    List<Object[]> findTopNBestSellingProductsForPeriod(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("topN") int topN
    );

    @Override
    @Query("SELECT EXTRACT(YEAR FROM o.orderDate), EXTRACT(MONTH FROM o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM o.orderDate), EXTRACT(MONTH FROM o.orderDate) " +
            "ORDER BY EXTRACT(YEAR FROM o.orderDate), EXTRACT(MONTH FROM o.orderDate)")
    List<Object[]> getSalesDynamicsByMonth(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Override
    @Query("SELECT EXTRACT(YEAR FROM o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM o.orderDate) " +
            "ORDER BY EXTRACT(YEAR FROM o.orderDate)")
    List<Object[]> getSalesDynamicsByYear(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Override
    @Query("SELECT o FROM Order o WHERE o.status = com.example.demo.enums.OrderStatus.CREATED AND o.orderDate < :dateThreshold")
    List<Order> findPendingPaymentOrdersOlderThan(@Param("dateThreshold") Date dateThreshold);
}
