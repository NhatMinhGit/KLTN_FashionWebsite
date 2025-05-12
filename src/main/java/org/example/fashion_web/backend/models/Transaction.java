package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "size_id", nullable = false)
    private Long sizeId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private TransactionGroup transactionGroup;


    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;


}
