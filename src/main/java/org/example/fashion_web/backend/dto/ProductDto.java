package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long productId;
    private Long brandId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private int stockQuantity;
}
