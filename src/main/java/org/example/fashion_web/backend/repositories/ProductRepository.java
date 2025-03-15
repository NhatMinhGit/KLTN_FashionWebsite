package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm sản phẩm theo ID (mặc định JpaRepository đã có findById)
    Optional<Product> findById(Long id);

    // Kiểm tra xem sản phẩm có tồn tại không
    boolean existsById(Long id);

    // Tìm kiếm sản phẩm theo tên (có thể áp dụng khi muốn tìm sản phẩm theo từ khóa)
    List<Product> findByNameContaining(String name);

    // Lọc sản phẩm theo khoảng giá
    List<Product> findByPriceBetween(Double min, Double max);
    List<Product> findProductsByCategoryName(String category);

    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds")
    List<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

}
