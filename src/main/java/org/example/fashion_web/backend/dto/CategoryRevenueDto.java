package org.example.fashion_web.backend.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategoryRevenueDto {
    private String name;
    private BigDecimal sales;

    public CategoryRevenueDto(String name, BigDecimal sales) {
        this.name = name;
        this.sales = sales;
    }

}
