package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DiscountService {
    ProductDiscount save(ProductDiscount productDiscount);

    Optional<ProductDiscount> getActiveDiscountForProduct(Product product);

    List<BigDecimal> getEffectivePrices(List<Product> products);

    BigDecimal applyDiscount(BigDecimal price, ProductDiscount discount);
}
