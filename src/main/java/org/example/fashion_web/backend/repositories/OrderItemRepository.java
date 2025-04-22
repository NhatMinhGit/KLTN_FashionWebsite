package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    //Get 10 top sale item
    @Query("SELECT new org.example.fashion_web.backend.dto.ProductRevenueDto(p.id, p.name, SUM(oi.pricePerUnit * oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 1 " +
            "GROUP BY p.id,p.name " +
            "ORDER BY SUM(oi.pricePerUnit * oi.quantity) DESC")
    List<ProductRevenueDto> findTopProductsByRevenue(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new org.example.fashion_web.backend.dto.CategoryRevenueDto(c.name, SUM(oi.pricePerUnit * oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 1 " +
            "GROUP BY c.name " +
            "ORDER BY SUM(oi.pricePerUnit * oi.quantity) DESC")
    List<CategoryRevenueDto> findTopCategoriesByRevenue(LocalDate startDate, LocalDate endDate);


    //Get 10 top lowest sale item
    @Query("SELECT new org.example.fashion_web.backend.dto.ProductRevenueDto(p.id, p.name, SUM(oi.pricePerUnit * oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 1 " +
            "GROUP BY p.id,p.name " +
            "ORDER BY SUM(oi.pricePerUnit * oi.quantity) ASC")
    List<ProductRevenueDto> findTopProductsByLowestRevenue(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new org.example.fashion_web.backend.dto.CategoryRevenueDto(c.name, SUM(oi.pricePerUnit * oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 1 " +
            "GROUP BY c.name " +
            "ORDER BY SUM(oi.pricePerUnit * oi.quantity) ASC")
    List<CategoryRevenueDto> findTopCategoriesByLowestRevenue(LocalDate startDate, LocalDate endDate);
}
