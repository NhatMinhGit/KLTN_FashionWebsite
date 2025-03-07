package org.example.fashion_web.backend.dto;


import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryForm {
    private Long category_id;
    private String category_name;
    private Long parent_category_id;
    private String parent_category_name;
    private String description;
}
