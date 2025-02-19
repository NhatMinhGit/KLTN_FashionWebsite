package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sale_amount", nullable = false, precision = 10, scale = 2 )
    private BigDecimal saleAmount;

    @Column(name = "note")
    private String note;

    @Column(name = "from_date", nullable = false)
    private LocalDateTime fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDateTime toDate;

}
