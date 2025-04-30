package org.example.fashion_web.frontend.controller;

import com.cloudinary.Cloudinary;
import org.example.fashion_web.backend.dto.*;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExcelImportController {

    @Autowired
    Cloudinary cloudinary;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private SizeService sizeService;
    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("/admin/datatree")
    public ModelAndView dataTree() {
        return new ModelAndView("product/data-tree");
    }

    @PostMapping("/admin/datatree/import")
    public ResponseEntity<String> importExcel(@RequestBody Map<String, List<Map<String, Object>>> data) {
        try {
//            if (data.containsKey("Category")) {
//                handleCategoryData(data.get("Category"));
//            }
//            if (data.containsKey("Brand")) {
//                handleBrandData(data.get("Brand"));
//            }
//            if (data.containsKey("Product")) {
//                handleProductData(data.get("Product"));
//            }
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


//    private void handleProductData(List<Map<String, Object>> rows) {
//        for (Map<String, Object> row : rows) {
//            ProductDto dto = mapToProductDto(row); // Chuyển đổi row thành ProductDto
//            List<Product> products = productService.findByName(dto.getName());
//            if(!products.isEmpty()){
//                Product productExisting = products.get(0); // Hoặc chọn sản phẩm theo logic riêng
//                int newQty = productExisting.getStockQuantity() + dto.getStock_quantity();
////                System.err.println("Quantity Updated to: "+ newQty);
//                productExisting.setStockQuantity(newQty);
////                System.out.println("Cộng thêm :" + dto.getStock_quantity());
//                productService.save(productExisting);
//            } else {
//                Product product = new Product();
//                product.setName(dto.getName());
//                product.setPrice(dto.getPrice());
//                product.setDescription(dto.getDescription());
////                product.setStockQuantity(dto.getStock_quantity());
//
//                // Lấy brand và category từ DB
//                Brand brand = brandService.findById(dto.getBrand_id())
//                        .orElseThrow(() -> new RuntimeException("Brand not found"));
//                Category category = categoryService.findById(dto.getCategory_id())
//                        .orElseThrow(() -> new RuntimeException("Category not found"));
//
//
//                product.setBrand(brand);
//                product.setCategory(category);
//                System.out.println("Product: " + product.getName() + ", Description: " + product.getDescription() + ", Price: " + product.getPrice() + ", Stock Quantity: " + product.getStockQuantity());
//
//                productService.save(product);
//            }
//            // === Xử lý Size ===
//            String sizeName = dto.getSizeName(); // cần có field này trong dto
//            Size existingSize = product.getSizes().stream()
//                    .filter(s -> s.getSizeName().equalsIgnoreCase(sizeName))
//                    .findFirst()
//                    .orElse(null);
//
//            if (existingSize != null) {
//                int newQty = existingSize.getStockQuantity() + dto.getStock_quantity();
//                existingSize.setStockQuantity(newQty);
//            } else {
//                Size newSize = new Size();
//                newSize.setSizeName(sizeName);
//                newSize.setStockQuantity(dto.getStock_quantity());
//                newSize.setProduct(product);
//                product.getSizes().add(newSize);
//            }
//        }
//    }
    private void handleProductData(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            ProductDto dto = mapToProductDto(row); // Chuyển đổi row thành ProductDto
            List<Product> products = productService.findByName(dto.getName()); // Tìm sản phẩm theo tên

            Product product;
            if (!products.isEmpty()) {
                product = products.get(0); // Lấy sản phẩm đã tồn tại (hoặc chọn sản phẩm theo logic khác)
                productService.save(product);
            } else {
                // Tạo sản phẩm mới nếu chưa có trong DB
                product = new Product();
                product.setName(dto.getName());
                product.setPrice(dto.getPrice());
                product.setDescription(dto.getDescription());

                // Lấy thông tin Brand và Category từ DB
                Brand brand = brandService.findById(dto.getBrand_id())
                        .orElseThrow(() -> new RuntimeException("Brand not found"));
                Category category = categoryService.findById(dto.getCategory_id())
                        .orElseThrow(() -> new RuntimeException("Category not found"));

                product.setBrand(brand);
                product.setCategory(category);

                // In thông tin sản phẩm ra để kiểm tra
                System.out.println("Product: " + product.getName() + ", Description: " + product.getDescription() + ", Price: " + product.getPrice() + ", Stock Quantity: " + product.getVariants());

                productService.save(product);
            }
            // Xử lý các kích cỡ cho sản phẩm
            processVariantsForProduct(dto.getProductVariants(), product);
        }

    }
    private void processVariantsForProduct(List<ProductVariantDto> variants, Product product) {
        if (variants != null) {
            for (ProductVariantDto variantDto : variants) {
                // Tìm variant theo màu
                ProductVariant existingVariant = product.getVariants().stream()
                        .filter(v -> v.getColor().equalsIgnoreCase(variantDto.getColor()))
                        .findFirst()
                        .orElse(null);

                if (existingVariant != null) {
                    // Nếu variant tồn tại, cập nhật size
                    // Thay vì lấy từ existingVariant.getSizes(), ta sử dụng sizeService để lấy sizes
                    List<Size> sizes = sizeService.findAllByProductVariantId(existingVariant.getId());

                    for (SizeDto sizeDto : variantDto.getSizes()) {
                        Size existingSize = sizes.stream()
                                .filter(s -> s.getSizeName().equalsIgnoreCase(sizeDto.getSizeName()))
                                .findFirst()
                                .orElse(null);

                        if (existingSize != null) {
                            // Cập nhật stockQuantity nếu size đã tồn tại
                            existingSize.setStockQuantity(existingSize.getStockQuantity() + sizeDto.getStockQuantity());
                        } else {
                            // Nếu size chưa tồn tại, tạo mới size
                            Size newSize = new Size();
                            newSize.setSizeName(sizeDto.getSizeName());
                            newSize.setStockQuantity(sizeDto.getStockQuantity());
                            newSize.setProductVariant(existingVariant); // Mối quan hệ với variant
                            sizeService.save(newSize); // Lưu vào cơ sở dữ liệu
                        }
                    }
                } else {
                    // Nếu variant chưa tồn tại → tạo mới
                    ProductVariant newVariant = new ProductVariant();
                    newVariant.setColor(variantDto.getColor());
                    newVariant.setProduct(product);

                    // Tạo và lưu các kích thước mới mà không cần thêm vào newVariant
                    for (SizeDto sizeDto : variantDto.getSizes()) {
                        Size newSize = new Size();
                        newSize.setSizeName(sizeDto.getSizeName());
                        newSize.setStockQuantity(sizeDto.getStockQuantity());
                        newSize.setProductVariant(newVariant); // Mối quan hệ với variant
                        sizeService.save(newSize); // Lưu vào cơ sở dữ liệu
                    }

                    // Không cần gán sizes vào newVariant nữa
                    product.getVariants().add(newVariant); // Thêm variant mới vào sản phẩm
                }
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
        String baseUrl = "https://res.cloudinary.com/dgtfqxgvx/image/upload/v1745988704";

        for (Map<String, Object> row : rows) {
            ImageDto dto = mapToImageDto(row); // Chuyển đổi row thành ImageDto
            Image image = new Image();
            image.setImageUri(baseUrl + encodeForCloudinary(dto.getImageUrl())); // Cộng thêm đường dẫn Cloudinary
            image.setImageName(dto.getImageName());

            // Lấy parent category từ DB
            ProductVariant productVariant = productVariantService.findById(dto.getProductVariantId()).orElse(null);
            if (productVariant == null) {
                System.out.println("Không tìm thấy Product Variant với ID: " + dto.getProductVariantId());
                continue; // Bỏ qua bản ghi lỗi
            }

            image.setProductVariant(productVariant);
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

//                // KHÔNG dùng orElseThrow nếu parentId là null
//                if (dto.getParentCategoryId() != null) {
//                    Optional<Category> parentCategory = categoryService.findById(dto.getParentCategoryId());
//                    if (parentCategory.isPresent()) {
//                        category.setParentCategory(parentCategory.get());
//                    } else {
//                        System.out.println("Không tìm thấy parent category id = " + dto.getParentCategoryId()
//                                + " cho category: " + dto.getCategoryName());
//                        category.setParentCategory(null); // fallback về null
//                    }
//                } else {
//                    category.setParentCategory(null); // category gốc
//                }

                System.out.println("→ Category: " + category.getName()
                        + ", Parent: " + (category.getParentCategory() != null ? category.getParentCategory().getName() : "null"));

                categoryService.save(category);
            }
        }
    }




    private CategoryDto mapToCategoryDto(Map<String, Object> row) {
        CategoryDto dto = new CategoryDto();
//        dto.setCategoryName(String.valueOf(row.get("category_name")));
//        dto.setDescription(String.valueOf(row.get("description")));
        dto.setCategoryName(getSafeString(row, "category_name"));
        dto.setDescription(getSafeString(row, "description"));


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
//        dto.setBrandName(String.valueOf(row.get("brand_name")));
//        dto.setDescription(String.valueOf(row.get("description")));
        dto.setBrandName(getSafeString(row, "brand_name"));
        dto.setDescription(getSafeString(row, "description"));

        return dto;
    }

    private ImageDto mapToImageDto(Map<String, Object> row) {
        ImageDto dto = new ImageDto();
//        dto.setProductVariantId(Long.valueOf((String.valueOf(row.get("product_variant_id")))));
//        dto.setImageUrl(String.valueOf(row.get("imageUri")));
//        dto.setImageName(String.valueOf(row.get("imageName")));

        dto.setImageUrl(getSafeString(row, "imageUri"));
        dto.setImageName(getSafeString(row, "imageName"));
        dto.setProductVariantId(parseLong(row.get("product_variant_id")));
        return dto;
    }
    private ProductDto mapToProductDto(Map<String, Object> row) {
        ProductDto dto = new ProductDto();
        dto.setBrand_id(Long.valueOf(String.valueOf(row.get("brand_id"))));
        dto.setCategory_id(Long.valueOf(String.valueOf(row.get("category_id"))));
        dto.setName(String.valueOf(row.get("name")));
//        dto.setPrice(BigDecimal.valueOf((Integer) row.get("price")));
        dto.setPrice(parseBigDecimal(row.get("price")));
        dto.setDescription(String.valueOf(row.get("description")));
//        dto.setStock_quantity((Integer) row.get("stock_quantity"));


        // Ánh xạ size từ row
        SizeDto sizeDto = mapToSizeDto(row);
        dto.setSizes(List.of(sizeDto)); // Thêm size vào danh sách nếu muốn chứa nhiều kích cỡ

        return dto;
    }

    private SizeDto mapToSizeDto(Map<String, Object> row) {
        SizeDto sizeDto = new SizeDto();
        sizeDto.setProductName(String.valueOf(row.get("product_name"))); // Tên sản phẩm (nếu cần)
        sizeDto.setSizeName(String.valueOf(row.get("size_name"))); // Lấy kích cỡ từ row
        sizeDto.setStockQuantity(parseInt(row.get("stock_quantity")));
        return sizeDto;
    }
    private String getSafeString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : "";
    }
    // Hàm chuẩn hóa tên sản phẩm thành slug
    public static String encodeForCloudinary(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return input;
        }
    }
    private Long parseLong(Object value) {
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    private BigDecimal parseBigDecimal(Object obj) {
        try {
            if (obj == null) return BigDecimal.ZERO;
            return new BigDecimal(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer parseInt(Object value) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }
}
