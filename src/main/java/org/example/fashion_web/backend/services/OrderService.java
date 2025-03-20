package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.OrderItem;

public interface OrderService {
    // Lưu order từ model
    Order save(Order Order);
}
