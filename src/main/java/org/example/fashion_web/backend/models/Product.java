package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Có thể giữ LAZY nếu sử dụng @EntityGraph
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY) // Có thể giữ LAZY nếu sử dụng @EntityGraph
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @Getter
    @Setter
    @Transient
    private BigDecimal effectivePrice;

    public Integer getProductDiscount() {
        if (effectivePrice != null && effectivePrice.compareTo(price) < 0) {
            BigDecimal discount = BigDecimal.ONE.subtract(effectivePrice.divide(price, 2, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100));
            return discount.setScale(0, RoundingMode.HALF_UP).intValue();
        }
        return 0;
    }

}
