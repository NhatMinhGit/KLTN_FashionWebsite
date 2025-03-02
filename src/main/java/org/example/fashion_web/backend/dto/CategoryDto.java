package org.example.fashion_web.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String categoryName;
    private String description;
    private Long parentCategoryId;
}

