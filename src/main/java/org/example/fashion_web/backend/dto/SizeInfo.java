package org.example.fashion_web.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SizeInfo {
    private Long variantId;      // Thêm biến này để chứa ID của variant
    private String sizeKey;      // Size (S, M, L, XL...)
    private String sizeValue;    // Ví dụ: "Small", "Medium", "Large"
    private int stockQuantity;   // Số lượng trong kho
}
