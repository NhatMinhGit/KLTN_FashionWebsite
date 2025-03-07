package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long product_id;
    private Long brand_id;
    private Long category_id;
    private String name;
    private BigDecimal price;
    private String description;
    private int stock_quantity;
}
