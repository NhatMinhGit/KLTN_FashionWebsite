package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.models.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // Lưu order từ model
    Order save(Order Order);
    List<BigDecimal> getRevenueByYear(int year);

    Map<Integer, BigDecimal> getRevenueByYears();

    int getTotalOrdersCount();

    List<OrderStatusDto> getOrderStatusData(int year, int month);

    int getOrdersToday();

    int getOrdersThisMonth();

}
