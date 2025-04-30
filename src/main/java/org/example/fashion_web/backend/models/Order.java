package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_status")
    private OrderStatusType status;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "payment_method")
    private String paymentMethod;

    public enum OrderStatusType {
        PAYING,PENDING, PAID, SHIPPED, COMPLETED, CANCELLED
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name="vnpay_id")
    private String vnpId;

}

