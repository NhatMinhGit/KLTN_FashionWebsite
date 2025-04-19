package org.example.fashion_web.frontend.controller;

import com.cloudinary.Cloudinary;
import org.example.fashion_web.backend.dto.*;
import org.example.fashion_web.backend.models.Brand;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.Size;
import org.example.fashion_web.backend.services.BrandService;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
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

    @GetMapping("/admin/datatree")
    public ModelAndView dataTree() {
        return new ModelAndView("product/data-tree");
    }

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
//            if (data.containsKey("Image")) {
//                handleImageData(data.get("Image"));
//            }
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
                System.out.println("Product: " + product.getName() + ", Description: " + product.getDescription() + ", Price: " + product.getPrice() + ", Stock Quantity: " + product.getSizes());

                productService.save(product);
            }
            // Xử lý các kích cỡ cho sản phẩm
            processSizesForProduct(dto.getSizes(), product);
        }

    }
    private void processSizesForProduct(List<SizeDto> sizes, Product product) {
        if (sizes != null) {
            for (SizeDto sizeDto : sizes) {
                // Kiểm tra xem kích cỡ đã tồn tại trong sản phẩm chưa
                Size existingSize = product.getSizes().stream()
                        .filter(s -> s.getSizeName().equalsIgnoreCase(sizeDto.getSizeName()))
                        .findFirst()
                        .orElse(null);

                if (existingSize != null) {
                    // Nếu kích cỡ đã có, cập nhật số lượng tồn kho
                    int newQty = existingSize.getStockQuantity() + sizeDto.getStockQuantity();
                    existingSize.setStockQuantity(newQty);
                } else {
                    // Nếu kích cỡ chưa có, thêm kích cỡ mới vào sản phẩm
                    Size newSize = new Size();
                    newSize.setSizeName(sizeDto.getSizeName());
                    newSize.setStockQuantity(sizeDto.getStockQuantity());
                    newSize.setProduct(product);
                    product.getSizes().add(newSize);
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

//    private void handleImageData(List<Map<String, Object>> rows) {
//        // Tương tự như handleProductData, bạn xử lý dữ liệu image ở đây
//        for (Map<String, Object> row : rows) {
//            ImageDto dto = mapToImageDto(row); // Chuyển đổi row thành ImageDto
//            Image image = new Image();
//            image.setImageUri(dto.getImageUrl());
//            image.setImageName(dto.getImageName());
//
//            // Lấy parent category từ DB
//            Product product = productService.findById(dto.getProductId())
//                    .orElseThrow(() -> new RuntimeException("Product not found"));
//
//            image.setProduct(product);
//            image.setImageSize(dto.getImageSize());
//            image.setImageType(dto.getImageType());
//            System.out.println("Image url: " + image.getImageUri() + ", Name: " + image.getImageName());
//
//            imageService.save(image); // Lưu ảnh vào DB
//
//        }
//    }
//    private void handleImageData(List<Map<String, Object>> rows) {
//        for (Map<String, Object> row : rows) {
//            ImageDto dto = mapToImageDto(row);  // Chuyển đổi row thành ImageDto
//
//            // Lấy sản phẩm từ DB
//            Product productForm = productService.findById(dto.getProductId())
//                    .orElseThrow(() -> new RuntimeException("Product not found"));
//
//            String productName = productForm.getName(); // Tên sản phẩm dùng để tạo folder
//
//            String imageName = dto.getImageName();  // Tên file ảnh từ DTO
//            String baseName = FilenameUtils.getBaseName(imageName);  // Tên cơ bản (không bao gồm phần mở rộng)
//            String folderPath = "pics/uploads/" + productName;
//
//            try {
//                // Giả sử bạn có đường dẫn URL hoặc file ảnh (dựa trên DTO chứa thông tin về ảnh)
//                String imageUrl = dto.getImageUrl();  // Đường dẫn URL của ảnh (hoặc đường dẫn tới file ảnh)
//                if(imageUrl == null){
//                    imageUrl = "/default.png";
//                }
//                String imageCloudinaryUrl = "https://res.cloudinary.com/dgtfqxgvx/image/upload/v1744871959"+ imageUrl;
//                if (imageUrl != null && !imageUrl.isEmpty()) {
//                    // Upload ảnh lên Cloudinary từ URL
//                    Map uploadResult = cloudinary.uploader().upload(imageCloudinaryUrl, ObjectUtils.asMap(
//                            "public_id", folderPath + "/" + baseName,
//                            "overwrite", true,
//                            "resource_type", "image"
//                    ));
//
//                    String cloudinaryUrl = (String) uploadResult.get("secure_url"); // Lấy URL ảnh đã upload
//
//                    // Tạo và lưu đối tượng Image
//                    Image image = new Image();
//                    image.setImageUri(cloudinaryUrl);
//                    image.setImageName(imageName);
//                    image.setProduct(productForm); // Gán đúng product
//                    image.setImageType(dto.getImageType());  // Lấy type từ DTO
//                    image.setImageSize(dto.getImageSize());  // Lấy size từ DTO
//
//                    imageService.save(image);  // Lưu ảnh vào DB
//
//                } else {
//                    System.err.println("Ảnh không hợp lệ hoặc không có URL.");
//                }
//
//            } catch (IOException e) {
//                System.err.println("Lỗi khi upload ảnh: " + imageName);
//                e.printStackTrace();
//            }
//        }
//    }


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
        sizeDto.setStockQuantity((Integer) row.get("stock_quantity")); // Lấy số lượng từ row

        return sizeDto;
    }

}
