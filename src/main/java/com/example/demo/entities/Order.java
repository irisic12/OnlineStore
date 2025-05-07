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
import java.util.ArrayList;
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

    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void setOrderDate(Date orderDate) {
        if (orderDate != null && orderDate.after(new Date())) {
            throw new IllegalArgumentException("Order date must be in the past or present.");
        }
        this.orderDate = orderDate;
    }

    // Метод для пересчета общей суммы
    @PostLoad
    @PostUpdate
    @PostPersist
    public void calculateTotal() {
        if (orderItems != null) {
            this.totalAmount = orderItems.stream()
                    .map(item -> item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }
}
