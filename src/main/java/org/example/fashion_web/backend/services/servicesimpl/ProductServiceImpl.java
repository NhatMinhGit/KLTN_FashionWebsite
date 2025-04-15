package org.example.fashion_web.backend.services.servicesimpl;


import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;



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

    // Xóa sản phẩm theo ID
    @Override
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm để xóa với ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // Tạo sản phẩm mới
    @Override
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    // Cập nhật thông tin sản phẩm
    @Override
    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());
            product.setStock_quantity(productDetails.getStock_quantity());
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


}



