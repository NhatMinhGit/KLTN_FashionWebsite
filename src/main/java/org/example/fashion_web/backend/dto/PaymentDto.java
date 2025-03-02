package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String paymentStatus;
}
