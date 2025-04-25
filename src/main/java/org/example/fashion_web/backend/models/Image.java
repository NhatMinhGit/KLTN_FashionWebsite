package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "product_id", nullable = false)
//    private Product product;
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "image_uri", nullable = false, length = 1024)
    private String imageUri;

    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "image_size")
    private Integer imageSize;

    @Column(name = "image_type", length = 50)
    private String imageType;
}
