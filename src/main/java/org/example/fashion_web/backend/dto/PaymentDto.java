package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String paymentStatus;
}
