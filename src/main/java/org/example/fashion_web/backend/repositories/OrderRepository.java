package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order save(Order order);
    List<Order> findByUser_Id(Long userId);
    // 1. Doanh thu từng tháng của một năm
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month")
    BigDecimal getMonthlyRevenue(@Param("year") int year, @Param("month") int month);

    // 1. Doanh thu từng tháng của một năm
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE YEAR(o.orderDate) = :year")
    BigDecimal getYearlyRevenue(@Param("year") int year);

    // 2. Doanh thu từng năm (distinct năm có đơn hàng)
    @Query("SELECT YEAR(o.orderDate) as year, SUM(o.totalPrice) as total FROM Order o GROUP BY YEAR(o.orderDate) ORDER BY YEAR(o.orderDate) ASC")
    List<Object[]> getRevenueByYears();
}
