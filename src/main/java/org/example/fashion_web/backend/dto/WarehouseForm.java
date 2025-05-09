package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseForm {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String description;
    private List<Long> variantIds;
//    private Map<Long, Map<String, Integer>> sizeQuantities;
    private Map<String, Integer> sizeQuantities;
    private List<BigDecimal> unitPrices;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String note;
    // getters, setters
}
