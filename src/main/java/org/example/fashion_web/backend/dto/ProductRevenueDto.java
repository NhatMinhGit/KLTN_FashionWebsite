package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRevenueDto {
    private Long id;
    private String name;
    private BigDecimal sales;

}
