package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
