package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private int quantity;
    private BigDecimal pricePerUnit;
}