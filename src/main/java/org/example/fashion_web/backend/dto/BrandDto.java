package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {
    private Long brandId;
    private String brandName;
    private String description;
}
