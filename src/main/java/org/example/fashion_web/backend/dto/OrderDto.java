package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private Long userId;
    private Long paymentId;
    private Long voucherId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime orderDate;
    private List<OrderItemDto> orderItems; // Danh sách các sản phẩm trong đơn hàng
}
