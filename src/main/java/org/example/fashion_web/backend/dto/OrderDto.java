package org.example.fashion_web.backend.dto;

import lombok.*;
import org.example.fashion_web.backend.models.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime created_at;
    private BigDecimal totalPrice;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private String voucherCode;
    private String vnpId;
}
