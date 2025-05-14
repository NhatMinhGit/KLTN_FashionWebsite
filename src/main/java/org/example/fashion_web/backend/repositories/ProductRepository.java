package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm sản phẩm theo ID (mặc định JpaRepository đã có findById)
    @EntityGraph(attributePaths = {"category","brand"})
    Optional<Product> findById(Long id);
    Optional<Product> findProductByName(String categoryName);

    List<Product> findByName(String productName);
    // Kiểm tra xem sản phẩm có tồn tại không
    boolean existsById(Long id);


    // Lọc sản phẩm theo khoảng giá
    List<Product> findByPriceBetween(Double min, Double max);
    List<Product> findProductsByCategoryName(String category);
    List<Product> findProductsByName (String product);

    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds")
    List<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
    //AI function

    // Tìm kiếm sản phẩm theo tên (có thể áp dụng khi muốn tìm sản phẩm theo từ khóa)
    List<Product> findByNameContaining(String name);
    // Tìm kiếm sản phẩm theo tên

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByNameContainingIgnoreCase(String keyword);


    List<Product> findByPriceBetween(long priceMin,long priceMax);

    List<Product>findByPriceBetweenAndCategoryName(long priceMin,long priceMax,String name);



    @Query("""
    SELECT DISTINCT p FROM Product p
    JOIN p.variants v
    JOIN v.sizes s
    JOIN p.category c
    WHERE (:color IS NULL OR v.color = :color)
      AND (:size IS NULL OR s.sizeName = :size)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (c.id IN :categoryIds)
    """)
    List<Product> filterProductsWithCategoryIds(
            @Param("color") String color,
            @Param("size") String size,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryIds") List<Long> categoryIds);



    @Query("""
    SELECT DISTINCT p FROM Product p
    JOIN p.variants v
    JOIN v.sizes s
    JOIN p.category c
    WHERE (:color IS NULL OR v.color = :color)
      AND (:size IS NULL OR s.sizeName = :size)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:category IS NULL OR c.name = :category)
    """)
    List<Product> filterProducts(@Param("color") String color,
                                 @Param("size") String size,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("category") String category);


}
