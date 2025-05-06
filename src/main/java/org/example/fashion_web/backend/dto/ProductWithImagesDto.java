package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithImagesDto {
    private Long id;
    private Long brand_id;
    private Long category_id;
    private String name;
    private BigDecimal price;
    private String description;
    private Map<String, Integer> sizeQuantities;
    private List<String> imageUrls;
    private List<ProductVariantDto> productVariants;
    private BigDecimal effectivePrice;  // Thêm thuộc tính này
    private Integer productDiscount;    // Thêm thuộc tính này
}