package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface ProductVariantService {

    List<ProductVariant> saveAll(List<ProductVariant> productVariants);

    List<ProductVariant> findAllByProductId(Long id);

    void updateVariant(Long variantId,ProductVariant updatedVariant);

    void save(ProductVariant productVariant);

    Optional<ProductVariant> findById(Long id);

    boolean deleteVariantById(Long variantId);
}
