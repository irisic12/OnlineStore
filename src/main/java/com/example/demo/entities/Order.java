package com.example.demo.entities;

import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date orderDate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    public void setOrderDate(Date orderDate) {
        if (orderDate != null && orderDate.after(new Date())) {
            throw new IllegalArgumentException("Order date must be in the past or present.");
        }
        this.orderDate = orderDate;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero.");
        }
        this.totalAmount = totalAmount;
    }
}
