package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    List<Order> findByUser_IdOrderByIdDesc(Long userId);

    List<Order> findAllByOrderByIdDesc();

    List<Order> findAllByOrderByIdDesc(Pageable pageable);


    // Truy vấn với SQL thuần túy cho các câu truy có null
//    @Query(value = "SELECT p.payment_status AS paymentStatus, COUNT(o.order_id) AS count " +
//            "FROM payment p " +
//            "LEFT JOIN orders o ON o.order_id = p.order_id " +
//            "WHERE YEAR(p.payment_date) = :year " +
//            "AND MONTH(p.payment_date) = :month " +
//            "GROUP BY p.payment_status", nativeQuery = true)
//    List<Object[]> findPaymentStatusCountByYearAndMonth(@Param("year") int year, @Param("month") int month);
    @Query(value = "SELECT p.payment_status AS paymentStatus, COUNT(o.order_id) AS count " +
            "FROM payment p " +
            "LEFT JOIN orders o ON o.order_id = p.order_id " +
            "WHERE YEAR(p.payment_date) = :year " +
            "AND (:month = 0 OR MONTH(p.payment_date) = :month) " +
            "GROUP BY p.payment_status", nativeQuery = true)
    List<Object[]> findPaymentStatusCountByYearAndMonth(@Param("year") int year, @Param("month") int month);


    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    Integer countOrdersToday();

    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
    Integer countOrdersThisMonth();

    List<Order> findByStatusIn(List<Order.OrderStatusType> statuses);

    List<Order> findByStatus(Order.OrderStatusType status);

    boolean existsByUserAndStatus(User user, Order.OrderStatusType status);

    @Query("SELECT o FROM Order o WHERE MONTH(o.orderDate) = MONTH(CURRENT_DATE) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE) AND o.user.id = :userId")
    List<Order> findOrdersInCurrentMonthByUser(@Param("userId") Long userId);


    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status IN :statuses")
    List<Order> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<Order.OrderStatusType> statuses);

    @Query("SELECT o FROM Order o WHERE MONTH(o.orderDate) = MONTH(CURRENT_DATE) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE)")
    List<Order> findAllOrdersInCurrentMonth();
}
