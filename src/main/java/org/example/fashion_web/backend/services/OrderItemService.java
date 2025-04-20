package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.OrderItem;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface OrderItemService {
    // Lưu order item từ model
    OrderItem save(OrderItem OrderItem);
    List<ProductRevenueDto> getTop10ProductsByRevenue(LocalDate monthStart, LocalDate monthEnd);

    List<CategoryRevenueDto> getTop10CategoriesByRevenue(LocalDate monthStart, LocalDate monthEnd);
}
