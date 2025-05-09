package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDto {
    private String status;
    private int count;
}
