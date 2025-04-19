package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.example.fashion_web.backend.repositories.DiscountRepository;
import org.example.fashion_web.backend.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;


    @Override
    public ProductDiscount save(ProductDiscount productDiscount) {
        return discountRepository.save(productDiscount);
    }

    @Override
    public Optional<ProductDiscount> getActiveDiscountForProduct(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findFirstByProductAndStartTimeBeforeAndEndTimeAfter(
                product, now, now
        );
    }
    @Override
    public List<BigDecimal> getEffectivePrices(List<Product> products) {
        List<BigDecimal> effectivePrices = new ArrayList<>();

        for (Product product : products) {
            BigDecimal effectivePrice = this.getActiveDiscountForProduct(product)
                    .map(discount -> this.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
            effectivePrices.add(effectivePrice);
        }

        return effectivePrices;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal price, ProductDiscount discount) {
        if (discount.getDiscountPercent() != null) {
            BigDecimal discountAmount = price
                    .multiply(BigDecimal.valueOf(discount.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
            return price.subtract(discountAmount);
        } else if (discount.getDiscountAmount() != null) {
            return price.subtract(discount.getDiscountAmount());
        }
        return price;
    }


}
