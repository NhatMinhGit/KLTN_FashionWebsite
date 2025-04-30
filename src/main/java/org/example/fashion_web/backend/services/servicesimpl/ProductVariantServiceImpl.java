package org.example.fashion_web.backend.services.servicesimpl;

import jakarta.persistence.EntityNotFoundException;
import org.example.fashion_web.backend.models.ProductVariant;
import org.example.fashion_web.backend.repositories.ProductVariantRepository;
import org.example.fashion_web.backend.services.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Override
    public List<ProductVariant> saveAll(List<ProductVariant> productVariants) {
        return productVariantRepository.saveAll(productVariants);
    }

    @Override
    public List<ProductVariant> findAllByProductId(Long id) {
        return productVariantRepository.findAllByProductId(id);
    }

    @Override
    public void updateVariant(Long variantId, ProductVariant updatedVariant) {
        Optional<ProductVariant> existingVariantOpt = productVariantRepository.findById(variantId);
        if (existingVariantOpt.isPresent()) {
            ProductVariant existingVariant = existingVariantOpt.get();
            existingVariant.setColor(updatedVariant.getColor());
//            existingVariant.setSizes(updatedVariant.getSizes()); // Cập nhật các size cho variant
            productVariantRepository.save(existingVariant);
        } else {
            throw new EntityNotFoundException("ProductVariant not found with id: " + variantId);
        }
    }

    @Override
    public void save(ProductVariant productVariant) {
        productVariantRepository.save(productVariant);
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return productVariantRepository.findById(id);
    }

    @Override
    @Transactional
    public boolean deleteVariantById(Long variantId) {
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
        if (variantOpt.isPresent()) {
            ProductVariant variant = variantOpt.get();
            productVariantRepository.delete(variant); // This will cascade delete associated Images
            return true;
        }
        return false;
    }


}
