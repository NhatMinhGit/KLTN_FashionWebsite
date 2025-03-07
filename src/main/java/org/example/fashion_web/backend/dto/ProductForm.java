package org.example.fashion_web.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductForm {
    //Thông tin sản phẩm
    private Long product_id;
    private Long brand_id;
    private Long category_id;
    private String name;
    private BigDecimal price;
    private String description;
    private int stock_quantity;

    //Thông tin thương hiệu
    private String brand_name;

    //Thông tin danh mục
    private String category_name;

}
