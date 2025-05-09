package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size,Long> {
//    Optional<Size> findByProductAndSizeName(Product product, String sizeName);

//    List<Size> findAllByProductId(Long id);

    List<Size> findByProductVariantId(Long variantId);

    Optional<Size> findByProductVariantIdAndSizeName(Long id, String name);

    Optional<Size> findBySizeName(String name);

    @Query("SELECT SUM(s.stockQuantity) FROM Size s")
    Integer sumStock();


    @Query("SELECT SUM(s.stockQuantity * p.price) " +
            "FROM Size s " +
            "JOIN s.productVariant v " +
            "JOIN v.product p")
    BigDecimal sumStockValue();
}
