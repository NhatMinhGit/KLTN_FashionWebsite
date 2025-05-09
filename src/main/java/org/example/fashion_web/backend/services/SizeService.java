package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SizeService {
    // Lưu kích cỡ từ model
    Size save(Size size);

    List<Size> saveAll(List<Size> sizes);

    void updateSize(Long id,Size size);


//    Optional<Size> findByProductAndSizeName(Product product, String sizeName);

    List<Size> findAllByProductVariantId(Long id);

    Optional<Size> findByProductVariantIdAndSizeName(Long id, String name);

    Optional<Size> getSizeIdByName(String name);

    void updateStockQuantity(Long variantId,String sizeName,int quantity);


    void increaseStock(Long productSizeId, int amount);

    boolean decreaseStock(Long productSizeId, int amount);

    Integer getTotalStock();

    BigDecimal getStockValue();
}
