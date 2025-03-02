package org.example.fashion_web.backend.dto;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDto {
    private Long orderId;
    private String shippingAddress;
    private String shippingStatus;
}
