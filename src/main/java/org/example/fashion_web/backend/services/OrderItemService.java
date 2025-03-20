package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.OrderItem;


public interface OrderItemService {
    // Lưu order item từ model
    OrderItem save(OrderItem OrderItem);
}
