package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private Long imageId;
    private Long productId;
    private String imageUri;
    private String imageName;
    private Integer imageSize;
    private String imageType;
}
