package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.Size;

import java.util.List;
import java.util.Optional;

public interface SizeService {
    // Lưu kích cỡ từ model
    Size save(Size size);

    List<Size> saveAll(List<Size> sizes);

    Optional<Size> findByProductAndSizeName(Product product, String sizeName);

    List<Size> findAllByProductId(Long id);
}
