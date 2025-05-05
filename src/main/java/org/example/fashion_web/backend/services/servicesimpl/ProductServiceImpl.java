package org.example.fashion_web.backend.services.servicesimpl;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryService categoryService;

//    @Autowired
//    private DiscountService discountService;


    // Lấy danh sách tất cả sản phẩm
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Lưu sản phẩm mới từ DTO
    @Override
    public Product save(Product product) {
        System.out.println("Product name: " + product);
        return productRepository.save(product);
    }

    // Tìm sản phẩm theo ID
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public Optional<Product> findOptByName(String name) {
        return productRepository.findProductByName(name);
    }

    public String extractPublicId(String imageUrl) {
        try {
            int index = imageUrl.indexOf("/upload/");
            if (index == -1) return null;

            String publicIdWithVersion = imageUrl.substring(index + 8); // bỏ /upload/
            String[] parts = publicIdWithVersion.split("/", 2);
            if (parts.length < 2) return null;

            String publicIdEncoded = parts[1];
            String publicIdDecoded = URLDecoder.decode(publicIdEncoded, StandardCharsets.UTF_8.name());

            // Cắt bỏ phần đuôi mở rộng như .jpg, .png
            int dotIndex = publicIdDecoded.lastIndexOf(".");
            if (dotIndex != -1) {
                publicIdDecoded = publicIdDecoded.substring(0, dotIndex);
            }

            return publicIdDecoded;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Xóa sản phẩm theo ID
    @Override
    public void deleteProductById(Long productId) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        // Lấy danh sách ảnh liên kết với sản phẩm
        List<Image> images = imageRepository.findAllByProductVariantId(productId);

        for (Image image : images) {
            String imageUrl = image.getImageUri();

            if (imageUrl != null && imageUrl.startsWith("https://res.cloudinary.com")) {
                try {
                    // Trích xuất public_id từ URL
                    String publicId = extractPublicId(imageUrl);

                    if (publicId != null) {
                        // Gọi Cloudinary xoá ảnh
                        Map<String, String> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                        System.out.println("Xóa ảnh Cloudinary [" + publicId + "]: " + result);
                    } else {
                        System.err.println("Không thể trích xuất public_id từ URL: " + imageUrl);
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi khi xoá ảnh từ Cloudinary: " + imageUrl);
                    e.printStackTrace();
                }
            }

            // Xoá bản ghi ảnh trong DB
            imageRepository.delete(image);
        }

        // Xoá sản phẩm khỏi DB
        productRepository.delete(product);
    }


    // Tạo sản phẩm mới
    @Override
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    // Cập nhật thông tin sản phẩm
    @Override
    public void updateProduct(Long id, Product productDetails) {
        productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());
//            product.setStockQuantity(productDetails.getStockQuantity());
//            // Cập nhật lại danh sách sizes (xóa cũ, thêm mới)
//            product.getSizes().clear(); // hoặc dùng cascade remove
//            product.getSizes().addAll(productDetails.getSizes());

            // Xóa hết variants cũ và thêm variants mới (bao gồm size bên trong)
            product.getVariants().clear(); // nếu cascade = ALL + orphanRemoval = true
            product.getVariants().addAll(productDetails.getVariants());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));
    }

    @Override
    public int getTotalProductsCount() {
        return productRepository.findAll().size();
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryName);

        // Kiểm tra xem danh mục có tồn tại không
        if (!optionalCategory.isPresent()) {
            return new ArrayList<>(); // Trả về danh sách rỗng nếu không tìm thấy
        }

        Category category = optionalCategory.get();
        List<Long> categoryIds = categoryService.getAllSubCategoryIds(category.getId());

        return productRepository.findByCategoryIds(categoryIds);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public List<Product> saveAll(List<Product> dataList) {
        return productRepository.saveAll(dataList);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> searchByKeyword(String keyword) {
        return productRepository.findByNameContaining(keyword); // Tìm kiếm sản phẩm theo tên
    }

    @Override
    public List<Product> findCategoryIdsByParentCategoryName(String color, String size, BigDecimal maxPrice,List<Long> categories) {
        return productRepository.filterProductsWithCategoryIds(color,size,maxPrice,categories);
    }

    @Override
    public List<Product> filterProducts(String color, String size, BigDecimal maxPrice, String category) {
        return productRepository.filterProducts(color,size,maxPrice, category);
    }


}



