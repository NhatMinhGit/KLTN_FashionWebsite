package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private Long imageId;
    private Long productVariantId;
    private String imageUrl;
    private String imageName;
    private Integer imageSize;
    private String imageType;
}
