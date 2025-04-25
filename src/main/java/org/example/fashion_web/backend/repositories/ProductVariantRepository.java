package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {

    List<ProductVariant> findAllByProductId(Long id);

    List<ProductVariant> findByProductId(Long productId);

}
