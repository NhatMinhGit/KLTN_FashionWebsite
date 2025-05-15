package org.example.fashion_web.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleOrderDto {
    private Long id;
    private String customerName;
    private String createdAt;
}
