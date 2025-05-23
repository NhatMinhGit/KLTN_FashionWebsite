package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // Lưu order từ model
    Order findOrderById(Long id);
    Order save(Order Order);
    List<BigDecimal> getRevenueByYear(int year);

    Map<Integer, BigDecimal> getRevenueByYears();

    int getTotalOrdersCount();

    List<OrderStatusDto> getOrderStatusData(int year, int month);

    int getOrdersToday();

    List<Order> findOrdersByUserAndStatusIn(User user, List<Order.OrderStatusType> statuses);

    int getOrdersThisMonth();

    void notifyNewOrders(List<Order> orders);

    List<Order> getAllOrders(Long userId);

    boolean hasPayingOrder(User user);

    int getTotalOrdersNotCancelled();

    BigDecimal getTotalRevenueCompletedOrders();

    List<Order> searchOrders(String keyword, Order.OrderStatusType status);

    List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate);
}
