package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "Vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long id;

    @Column(name = "voucher_code", length = 50, unique = true, nullable = false)
    private String voucherCode;

    @Column(name = "voucher_name", length = 255, nullable = false)
    private String voucherName;

    @Column(name = "discount_type", length = 50, nullable = false)
    private String discountType; // "percentage" hoặc "fixed"

    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 1;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public Voucher(Integer usageLimit, LocalDate endDate, LocalDate startDate, BigDecimal minOrderValue, BigDecimal discountValue, String discountType, String voucherName, String voucherCode) {
        this.usageLimit = usageLimit;
        this.endDate = endDate;
        this.startDate = startDate;
        this.minOrderValue = minOrderValue;
        this.discountValue = discountValue;
        this.discountType = discountType;
        this.voucherName = voucherName;
        this.voucherCode = voucherCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
