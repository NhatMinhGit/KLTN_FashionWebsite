package org.example.fashion_web.frontend.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.sun.jdi.LongValue;
import org.example.fashion_web.backend.dto.BrandDto;
import org.example.fashion_web.backend.dto.CategoryDto;
import org.example.fashion_web.backend.dto.ImageDto;
import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Brand;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.services.BrandService;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExcelImportController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ImageService imageService;

    @GetMapping("/admin/datatree")
    public ModelAndView dataTree() {
        return new ModelAndView("product/data-tree");
    }

//    @PostMapping("/admin/datatree/import")
//    public ResponseEntity<String> importExcel(@RequestBody Map<String, List<Map<String, Object>>> data) {
//        // Duyệt qua từng sheet trong dữ liệu nhận được
//        data.forEach((sheetName, rows) -> {
//            // Tùy theo sheetName, bạn sẽ xử lý dữ liệu tương ứng (Product, Brand, Category,...)
//            try {
//                if ("Product".equals(sheetName)) {
//                    handleProductData(rows);
//                } else if ("Category".equals(sheetName)) {
//                    handleCategoryData(rows);
//                } else if ("Brand".equals(sheetName)) {
//                    handleBrandData(rows);
//                } else if ("Image".equals(sheetName)) {
//                    handleImageData(rows);
//                }
//            } catch (Exception e) {
//                System.err.println("Lỗi xử lý sheet " + sheetName + ": " + e.getMessage());
//            }
//            // Có thể tiếp tục thêm các sheet khác nếu cần
//        });
//        return ResponseEntity.ok("Đã import " + data.size() + " sheets.");
//    }
    @PostMapping("/admin/datatree/import")
    public ResponseEntity<String> importExcel(@RequestBody Map<String, List<Map<String, Object>>> data) {
        try {
            if (data.containsKey("Category")) {
                handleCategoryData(data.get("Category"));
            }
            if (data.containsKey("Brand")) {
                handleBrandData(data.get("Brand"));
            }
            if (data.containsKey("Product")) {
                handleProductData(data.get("Product"));
            }
            if (data.containsKey("Image")) {
                handleImageData(data.get("Image"));
            }
            // Có thể thêm sheet khác theo thứ tự mong muốn
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi import dữ liệu: " + e.getMessage());
        }

        return ResponseEntity.ok("Đã import " + data.size() + " sheets.");
    }


    private void handleProductData(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            ProductDto dto = mapToProductDto(row); // Chuyển đổi row thành ProductDto
            List<Product> products = productService.findByName(dto.getName());
            if(!products.isEmpty()){
                Product productExisting = products.get(0); // Hoặc chọn sản phẩm theo logic riêng
                int newQty = productExisting.getStock_quantity() + dto.getStock_quantity();
//                System.err.println("Quantity Updated to: "+ newQty);
                productExisting.setStock_quantity(newQty);
//                System.out.println("Cộng thêm :" + dto.getStock_quantity());
                productService.save(productExisting);
            } else {
                Product product = new Product();
                product.setName(dto.getName());
                product.setPrice(dto.getPrice());
                product.setDescription(dto.getDescription());
                product.setStock_quantity(dto.getStock_quantity());

                // Lấy brand và category từ DB
                Brand brand = brandService.findById(dto.getBrand_id())
                        .orElseThrow(() -> new RuntimeException("Brand not found"));
                Category category = categoryService.findById(dto.getCategory_id())
                        .orElseThrow(() -> new RuntimeException("Category not found"));


                product.setBrand(brand);
                product.setCategory(category);
                System.out.println("Product: " + product.getName() + ", Description: " + product.getDescription() + ", Price: " + product.getPrice() + ", Stock Quantity: " + product.getStock_quantity());

                productService.save(product);
            }

        }
    }

    private void handleBrandData(List<Map<String, Object>> rows) {
        // Tương tự như handleBrandData, bạn xử lý dữ liệu brand ở đây
        for (Map<String, Object> row : rows) {
            BrandDto dto = mapToBrandDto(row); // Chuyển đổi row thành ProductDto
            Optional<Brand> brandExisting = brandService.findByName(dto.getBrandName());
            if(brandExisting.isPresent()){
                System.out.println("Brand đã tồn tại: " + brandExisting.get().getName());
            } else {
                Brand brand = new Brand();

                brand.setName(dto.getBrandName());
                brand.setDescription(dto.getDescription());

                System.out.println("Brand: " + brand.getName() + ", Description: " + brand.getDescription());

                brandService.save(brand);
            }
        }
    }

    private void handleImageData(List<Map<String, Object>> rows) {
        // Tương tự như handleProductData, bạn xử lý dữ liệu image ở đây
        for (Map<String, Object> row : rows) {
            ImageDto dto = mapToImageDto(row); // Chuyển đổi row thành ImageDto
            Image image = new Image();
            image.setImageUri(dto.getImageUrl());
            image.setImageName(dto.getImageName());

            // Lấy parent category từ DB
            Product product = productService.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            image.setProduct(product);
            image.setImageSize(dto.getImageSize());
            image.setImageType(dto.getImageType());
            System.out.println("Image url: " + image.getImageUri() + ", Name: " + image.getImageName());

            imageService.save(image); // Lưu ảnh vào DB

        }
    }
    private void handleCategoryData(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            CategoryDto dto = mapToCategoryDto(row);
            Optional<Category> categoryExisting = categoryService.findByName(dto.getCategoryName());

            if (categoryExisting.isPresent()) {
                System.out.println("Category đã tồn tại: " + categoryExisting.get().getName());
            } else {
                Category category = new Category();
                category.setName(dto.getCategoryName());
                category.setDescription(dto.getDescription());

                // KHÔNG dùng orElseThrow nếu parentId là null
                if (dto.getParentCategoryId() != null) {
                    Optional<Category> parentCategory = categoryService.findById(dto.getParentCategoryId());
                    if (parentCategory.isPresent()) {
                        category.setParentCategory(parentCategory.get());
                    } else {
                        System.out.println("Không tìm thấy parent category id = " + dto.getParentCategoryId()
                                + " cho category: " + dto.getCategoryName());
                        category.setParentCategory(null); // fallback về null
                    }
                } else {
                    category.setParentCategory(null); // category gốc
                }

                System.out.println("→ Category: " + category.getName()
                        + ", Parent: " + (category.getParentCategory() != null ? category.getParentCategory().getName() : "null"));

                categoryService.save(category);
            }
        }
    }


    private CategoryDto mapToCategoryDto(Map<String, Object> row) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName(String.valueOf(row.get("category_name")));
        dto.setDescription(String.valueOf(row.get("description")));

        Object parentIdObj = row.get("parent_category_id");
        if (parentIdObj != null && !String.valueOf(parentIdObj).isBlank()) {
            try {
                dto.setParentCategoryId(Long.valueOf(String.valueOf(parentIdObj)));
            } catch (NumberFormatException e) {
                dto.setParentCategoryId(null); // fallback nếu lỗi dữ liệu
            }
        } else {
            dto.setParentCategoryId(null);
        }
        return dto;
    }


    private BrandDto mapToBrandDto(Map<String, Object> row) {
        BrandDto dto = new BrandDto();
        dto.setBrandName(String.valueOf(row.get("brand_name")));
        dto.setDescription(String.valueOf(row.get("description")));
        return dto;
    }

    private ImageDto mapToImageDto(Map<String, Object> row) {
        ImageDto dto = new ImageDto();
        dto.setProductId(Long.valueOf((String.valueOf(row.get("product_id")))));
        dto.setImageUrl(String.valueOf(row.get("imageUri")));
        dto.setImageName(String.valueOf(row.get("imageName")));

        return dto;
    }
    private ProductDto mapToProductDto(Map<String, Object> row) {
        ProductDto dto = new ProductDto();
        dto.setBrand_id(Long.valueOf(String.valueOf(row.get("brand_id"))));
        dto.setCategory_id(Long.valueOf(String.valueOf(row.get("category_id"))));
        dto.setName(String.valueOf(row.get("name")));
        dto.setPrice(BigDecimal.valueOf((Integer) row.get("price")));
        dto.setDescription(String.valueOf(row.get("description")));
        dto.setStock_quantity((Integer) row.get("stock_quantity"));

        return dto;
    }

}
