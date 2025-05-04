package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {

    List<ProductVariant> findAllByProductId(Long id);

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findById(Long id);

    void deleteById(Long id);
    boolean existsById(Long id);

    @Query("SELECT DISTINCT v.color FROM ProductVariant v WHERE v.color IS NOT NULL")
    List<String> findAllVariantColorsExist();
}
