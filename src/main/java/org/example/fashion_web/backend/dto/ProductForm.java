package org.example.fashion_web.backend.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    //Thông tin thương hiệu
    private String brand_name;

    //Thông tin danh mục
    private String category_name;

    // Thông tin danh sách ảnh
    private List<MultipartFile> imageFile;

    private List<String> imageColors = new ArrayList<>();

    // Thêm danh sách size và quantity
    private Map<String, Integer> sizeQuantities = new HashMap<>();


    // Thêm thông tin về variant (bao gồm màu sắc và danh sách size với số lượng)
//    private Map<String, Map<String, Integer>> variantSizes = new HashMap<>(); // Key là màu sắc, value là Map của size -> quantity
    private Map<Long, Map<String, Integer>> variantSizes = new HashMap<>(); // Key là màu sắc, value là Map của size ID -> quantity

    private List<String> color;

}
