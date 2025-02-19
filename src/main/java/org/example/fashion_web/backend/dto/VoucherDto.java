package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long voucherId;
    private Long orderId;
    private String title;
    private BigDecimal saleAmount;
    private String note;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}