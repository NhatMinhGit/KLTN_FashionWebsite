package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RevenueStatisticDto {
    private List<String> labels;
    private List<BigDecimal> data;
    private String label;

    // Constructors, getters, setters
}
