package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<ProductDiscount,Long> {

    Optional<ProductDiscount> findFirstByProductAndStartTimeBeforeAndEndTimeAfter(Product product, LocalDateTime startTime, LocalDateTime endTime);

    Optional<ProductDiscount> findById(Long id);

    List<ProductDiscount> findByProductIdAndActiveTrue(Long id);

    Optional<ProductDiscount> findProductDiscountByActiveAndCategory_Name(boolean active, String name);

    List<ProductDiscount> findByCategoryIdAndActiveTrue(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE ProductDiscount d SET d.active = true WHERE CURRENT_TIMESTAMP BETWEEN d.startTime AND d.endTime")
    void activateValidDiscounts();

    @Modifying
    @Transactional
    @Query("UPDATE ProductDiscount d SET d.active = false WHERE CURRENT_TIMESTAMP NOT BETWEEN d.startTime AND d.endTime")
    void deactivateInvalidDiscounts();

    @EntityGraph(attributePaths = {"product", "category"})
    Page<ProductDiscount> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT d FROM ProductDiscount d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ProductDiscount> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
