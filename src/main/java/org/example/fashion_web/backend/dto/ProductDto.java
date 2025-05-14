package org.example.fashion_web.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private Long brand_id;
    private Long category_id;
    private String categoryName;
    private String brandName;
    private String name;
    private BigDecimal price;
    private String description;
    private List<SizeDto> sizes; // Thêm danh sách các kích cỡ vào ProductDto
    private List<ProductVariantDto> productVariants;
}
