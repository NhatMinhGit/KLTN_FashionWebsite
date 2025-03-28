package org.example.fashion_web.backend.dto;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.example.fashion_web.backend.models.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private User user;
    private LocalDate created_at;
    private BigDecimal totalPrice;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
}
