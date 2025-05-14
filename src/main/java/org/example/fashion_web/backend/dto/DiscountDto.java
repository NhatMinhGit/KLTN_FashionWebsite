package org.example.fashion_web.backend.dto;

import lombok.*;
import org.example.fashion_web.backend.models.ProductDiscount;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DiscountDto {
    private Long id;
    private String name;
    private Integer discountPercent;
    private String productName;
    private String categoryName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public DiscountDto(ProductDiscount discount) {
        this.id = discount.getId();
        this.name = discount.getName();
        this.discountPercent = discount.getDiscountPercent();
        this.startTime = discount.getStartTime();
        this.endTime = discount.getEndTime();

        // PHẢI kiểm tra null trước khi gọi getName()
        this.productName = (discount.getProduct() != null) ? discount.getProduct().getName() : null;
        this.categoryName = (discount.getCategory() != null) ? discount.getCategory().getName() : null;
    }
}
