package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Size;

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

    void increaseStock(Long productSizeId, int amount);

    boolean decreaseStock(Long productSizeId, int amount);
}
