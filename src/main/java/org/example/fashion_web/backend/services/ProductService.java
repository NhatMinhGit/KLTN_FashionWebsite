package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface ProductService {
    List<Product> getAllProduct();
    Page<Product> getAllProduct(Pageable pageable);
    Product save(ProductDto productDto);

    Product getProductById(Long id);
    void deleteProductById(Long id);

    void createProduct(Product product);

}
