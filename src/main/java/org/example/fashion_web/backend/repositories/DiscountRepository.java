package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<ProductDiscount,Long> {

    Optional<ProductDiscount> findFirstByProductAndStartTimeBeforeAndEndTimeAfter(Product product, LocalDateTime startTime, LocalDateTime endTime);
}
