package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

//    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    private List<Size> sizes = new ArrayList<>();
//

}
