package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
public class CategoryRevenueDto {
    private String name;
    private BigDecimal sales;

    public CategoryRevenueDto(String name, BigDecimal sales) {
        this.name = name;
        this.sales = sales;
    }

}
