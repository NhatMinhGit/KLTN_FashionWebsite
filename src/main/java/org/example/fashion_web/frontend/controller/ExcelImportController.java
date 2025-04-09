package org.example.fashion_web.frontend.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.sun.jdi.LongValue;
import org.example.fashion_web.backend.dto.CategoryDto;
import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Brand;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.services.BrandService;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class ExcelImportController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/admin/datatree")
    public ModelAndView dataTree() {
        return new ModelAndView("product/data-tree");
    }

    @PostMapping("/admin/datatree/import")
    public ResponseEntity<String> importExcel(@RequestBody Map<String, List<Map<String, Object>>> data) {
        // Duyệt qua từng sheet trong dữ liệu nhận được
        data.forEach((sheetName, rows) -> {
            // Tùy theo sheetName, bạn sẽ xử lý dữ liệu tương ứng (Product, Brand, Category,...)
            if ("Product".equals(sheetName)) {
                handleProductData(rows);
            } else if ("Brand".equals(sheetName)) {
                handleBrandData(rows);
            } else if ("Category".equals(sheetName)) {
                handleCategoryData(rows);
            }
            // Có thể tiếp tục thêm các sheet khác nếu cần
        });

        return ResponseEntity.ok("Đã import " + data.size() + " sheets.");
    }

    private void handleProductData(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            ProductDto dto = mapToProductDto(row); // Chuyển đổi row thành ProductDto
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

    private void handleBrandData(List<Map<String, Object>> rows) {
        // Tương tự như handleProductData, bạn xử lý dữ liệu brand ở đây
    }

    private void handleCategoryData(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            CategoryDto dto = mapToCategoryDto(row); // Chuyển đổi row thành ProductDto
            Category category = new Category();
            category.setName(dto.getCategoryName());
            category.setDescription(dto.getDescription());

            // Lấy parent category từ DB
            Category categoryParent = categoryService.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            category.setParentCategory(categoryParent);
            System.out.println("Category: " + category.getName() + ", Description: " + category.getDescription() + ", Parent category: " + category.getParentCategory().getName());

            categoryService.save(category);
        }
    }

    private CategoryDto mapToCategoryDto(Map<String, Object> row) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName(String.valueOf(row.get("category_name")));
        dto.setDescription(String.valueOf(row.get("description")));
        dto.setParentCategoryId(Long.valueOf(String.valueOf(row.get("parent_category_id"))));
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
