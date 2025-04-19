package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product_discounts")
public class ProductDiscount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_discount_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;


    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "discount_amount", nullable = true)
    private BigDecimal discountAmount;
    @Column(name = "discount_percent", nullable = true)
    private Integer discountPercent;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "active", nullable = false)
    private boolean active;



}
