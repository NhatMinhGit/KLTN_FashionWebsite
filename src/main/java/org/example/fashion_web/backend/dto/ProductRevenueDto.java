package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRevenueDto {
    private Long id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal sales;
}
