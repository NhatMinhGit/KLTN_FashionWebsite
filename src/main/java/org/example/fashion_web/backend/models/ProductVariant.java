package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "color", nullable = false)
    private String color;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Size> sizes;
}
