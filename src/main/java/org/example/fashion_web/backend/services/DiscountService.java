package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.example.fashion_web.backend.dto.DiscountDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiscountService {
    ProductDiscount save(ProductDiscount productDiscount);
    Optional<ProductDiscount> findById(Long id);

    Optional<ProductDiscount> getActiveDiscountForProduct(Product product);

    List<ProductDiscount> getActiveDiscountsForProduct(Product product);

    List<ProductDiscount> getActiveDiscountsForCategory(Category category);

    List<BigDecimal> getEffectivePrices(List<Product> products);

    BigDecimal applyDiscount(BigDecimal price, ProductDiscount discount);

    void applyDiscountFor10LowSellingProducts(LocalDate startDate, LocalDate endDate);
    void applyDiscountFor3LowSellingCategories(LocalDate startDate, LocalDate endDate);
    int getTotalDiscountsCount();

//    Page<ProductDiscount> getAllDiscountsSorted(Pageable pageable);

    void updateDiscountStatuses();


    Page<DiscountDto> searchDiscounts(String keyword, Pageable pageable);

    List<ProductDiscount> getAllDiscounts();

    Page<ProductDiscount> getAllDiscounts(Pageable pageable);

    Optional<ProductDiscount> getDiscountById(Long id);

    void updateDiscount(Long id,ProductDiscount discountDetails);

    Optional<ProductDiscount> getActiveDiscountForCategory(Boolean active,String name);

}
