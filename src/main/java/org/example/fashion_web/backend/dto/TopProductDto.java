package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
    private String productName;
    private BigDecimal revenue;

    // Constructors, getters, setters
}
