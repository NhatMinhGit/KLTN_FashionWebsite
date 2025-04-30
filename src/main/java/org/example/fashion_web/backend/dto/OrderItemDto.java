package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private String pricePerUnit;


}
