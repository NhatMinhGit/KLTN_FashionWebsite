package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private LocalDate orderDate;
    private BigDecimal totalPrice;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
}
