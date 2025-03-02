package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long voucherId;
    private String voucherCode;
    private String voucherName;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int usageLimit;
}
