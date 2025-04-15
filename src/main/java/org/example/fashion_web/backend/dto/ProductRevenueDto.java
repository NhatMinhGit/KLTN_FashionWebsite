package org.example.fashion_web.backend.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRevenueDto {
    private String name;
    private BigDecimal sales;

    public ProductRevenueDto(String name, BigDecimal sales) {
        this.name = name;
        this.sales = sales;
    }

}
