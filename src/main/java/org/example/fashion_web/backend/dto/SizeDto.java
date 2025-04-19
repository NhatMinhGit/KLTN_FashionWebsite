package org.example.fashion_web.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SizeDto {
    private String productName; // để liên kết với product
    private String sizeName;
    private int stockQuantity;
}
