package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    // Lấy danh sách tất cả sản phẩm
    List<Product> getAllProducts();

    // Lấy danh sách sản phẩm có phân trang
    Page<Product> getAllProducts(Pageable pageable);

    // Lưu sản phẩm từ model
    Product save(Product product);

    // Tìm sản phẩm theo ID
    Optional<Product> getProductById(Long id);

    // Kiểm tra sản phẩm có tồn tại không
    boolean existsById(Long id);

    // Xóa sản phẩm theo ID
    void deleteProductById(Long id);

    // Tạo sản phẩm mới
    void addProduct(Product product);

    // Cập nhật thông tin sản phẩm
    Product updateProduct(Long id, Product productDetails);

    int getTotalProductsCount();
}
